package com.lmcysdz.taczrpg.network;
import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import com.lmcysdz.taczrpg.api.resource.MaterialQuality;
import com.lmcysdz.taczrpg.api.resource.ResourceCostSystem;
import com.lmcysdz.taczrpg.capability.AffixLibraryCapability;
import com.lmcysdz.taczrpg.registry.ModItems;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.List;
import java.util.function.Supplier;
/**
 * 客户端 → 服务端的校准站操作包。
 *
 * <p>操作类型：{@link Action#EXTRACT} 提取（枪销毁，无消耗）、
 * {@link Action#CALIBRATE} 校准（消耗递增）、
 * {@link Action#OPTIMIZE} 优化（消耗 ModuleItem，+1.5%）、
 * {@link Action#DISASSEMBLE} 分解（枪销毁，产材料，不入词条库）。</p>
 */

public class CalibrationActionPacket {
    public enum Action {       EXTRACT, CALIBRATE, OPTIMIZE, DISASSEMBLE   }
    private final Action action;
    private final String affixKey;
    /** 校准目标槽位（仅 CALIBRATE 使用，-1 = 未指定） */
    private final int slot;
    public CalibrationActionPacket(Action action, String affixKey) {
        this(action, affixKey, -1);
    }
    public CalibrationActionPacket(Action action, String affixKey, int slot) {
        this.action = action;
        this.affixKey = affixKey == null ? "" : affixKey;
        this.slot = slot;
    }

    // ===== 编解码 =====

    public static void encode(CalibrationActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUtf(msg.affixKey);
        buf.writeInt(msg.slot);
    }

    public static CalibrationActionPacket decode(FriendlyByteBuf buf) {
        return new CalibrationActionPacket(buf.readEnum(Action.class), buf.readUtf(), buf.readInt());
    }

    public static void handle(CalibrationActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            int result;
            switch (msg.action) {
                case EXTRACT -> result = doExtract(player, msg.affixKey);
                case CALIBRATE -> result = doCalibrate(player, msg.affixKey, msg.slot);
                case OPTIMIZE -> result = doOptimize(player, msg.affixKey);
                case DISASSEMBLE -> result = doDisassemble(player);
                default -> result = -1;
            }
            ModNetwork.sendToPlayer(player, new CalibrationResultPacket(msg.action, result));
        });
        ctx.get().setPacketHandled(true);
    }

    // ===== 提取：选一条词条 → 枪销毁 → 该词条最高值入库，无消耗 =====

    private static int doExtract(ServerPlayer player, String affixKey) {
        ItemStack gun = player.getMainHandItem();
        if (!AffixSystem.isGun(gun)) return 1;
        // 奇特武器无法提取词条
        if (ExoticWeaponManager.isExotic(gun)) return 4;

        AffixType type = AffixType.byKey(affixKey);
        if (type == null || !AffixSystem.getAttrKeys(gun.getOrCreateTag()).contains(type.key())) return 2;

        // 校准过的词条不可提取
        int index = AffixSystem.getAttrKeys(gun.getOrCreateTag()).indexOf(type.key());
        if (gun.getOrCreateTag().getInt(AffixSystem.CALIBRATED_SLOT_TAG) == index) return 3;

        // 提取数值
        double value = gun.getOrCreateTag().getDouble(type.key());

        // 存入词条库（只存绝对值）
        AffixLibraryCapability.get(player).ifPresent(lib ->
                lib.put(type.key(), (float) Math.abs(value))
        );

        // 枪销毁
        gun.shrink(1);

        // 同步词条库
        syncLibrary(player);
        return 0;
    }

    // ===== 校准：从词条库读 → 替换枪上对应位 → 消耗递增 =====

    private static int doCalibrate(ServerPlayer player, String affixKey, int slot) {
        ItemStack gun = player.getMainHandItem();
        if (!AffixSystem.isGun(gun)) return 1;
        // 奇特武器无法校准
        if (ExoticWeaponManager.isExotic(gun)) return 8;

        // 从词条库读取
        Float libraryValue = AffixLibraryCapability.get(player)
                .map(lib -> lib.get(affixKey))
                .orElse(null);
        if (libraryValue == null) return 2;

        AffixType libType = AffixType.byKey(affixKey);
        if (libType == null) return 4;

        CompoundTag gunTag = gun.getOrCreateTag();
        List<String> attrKeys = AffixSystem.getAttrKeys(gunTag);
        int calibratedSlot = gunTag.getInt(AffixSystem.CALIBRATED_SLOT_TAG);

        // 目标槽位校验（DESIGN：先选择枪上待替换的词条位）
        if (slot < 0 || slot >= attrKeys.size()) return 6;

        // 锁定规则：每把枪仅锁定 1 个词条位，只能替换该位
        if (calibratedSlot != -1 && calibratedSlot != slot) return 5;

        // 已校准槽可替换为词条库中任意词条（跨分类允许，贴合 Division 2 体验）
        String oldKey = attrKeys.get(slot);
        AffixType oldType = AffixType.byKey(oldKey);

        // 消耗计算 —— 按品质档 + 校准次数递增
        int rankIndex = AffixSystem.getRankIndex(gunTag.getInt(AffixSystem.LEVEL_TAG));
        int timesCalibrated = gunTag.getInt(AffixSystem.CALIBRATED_COUNT_TAG);
        int totalCost = ResourceCostSystem.getCalibrationTotalCost(rankIndex, timesCalibrated);
        Item tierMat = ResourceCostSystem.getTierMaterial(gun);

        // 材料检查（按枪械档位材料）
        if (ResourceCostSystem.countItem(player, tierMat) < totalCost) return 3;

        // 执行替换：移除旧词条值 → 写入库词条 → 更新槽位映射 → 锁定该位
        if (oldType != null) gunTag.remove(oldType.key());
        gunTag.putDouble(libType.key(), libraryValue);
        attrKeys.set(slot, libType.key());
        AffixSystem.setAttrKeys(gunTag, attrKeys.stream().map(AffixType::byKey).filter(java.util.Objects::nonNull).toList());
        gunTag.putInt(AffixSystem.CALIBRATED_SLOT_TAG, slot);

        // 扣除材料 + 递增校准次数
        ResourceCostSystem.tryConsumeItem(player, tierMat, totalCost);
        gunTag.putInt(AffixSystem.CALIBRATED_COUNT_TAG, timesCalibrated + 1);

        return 0;
    }

    // ===== 优化：指定词条数值 +1.5% → 满值15%，消耗 ModuleItem =====

    private static int doOptimize(ServerPlayer player, String affixKey) {
        ItemStack gun = player.getMainHandItem();
        if (!AffixSystem.isGun(gun)) return 1;

        AffixType type = AffixType.byKey(affixKey);
        if (type == null) return 2;

        CompoundTag tag = gun.getOrCreateTag();
        if (!tag.contains(type.key())) return 1;

        List<String> keys = AffixSystem.getAttrKeys(tag);
        int index = keys.indexOf(type.key());
        int calibratedSlot = tag.getInt(AffixSystem.CALIBRATED_SLOT_TAG);
        if (calibratedSlot == index) return 2; // 已锁定不可优化

        boolean exotic = ExoticWeaponManager.isExotic(gun);
        // 消耗检查（奇特：终极材料 + 模组数据块；普通：档位材料 + 模组数据块）
        if (exotic) {
            if (ResourceCostSystem.countItem(player, ModItems.MATERIALS.get(MaterialQuality.ULTIMATE).get()) < ResourceCostSystem.EXOTIC_OPTIMIZE_ULTIMATE
                    || ResourceCostSystem.countItem(player, ModItems.MODULE_ITEM.get()) < ResourceCostSystem.EXOTIC_OPTIMIZE_MODULE) return 3;
        } else {
            Item tierMat = ResourceCostSystem.getTierMaterial(gun);
            if (ResourceCostSystem.countItem(player, tierMat) < ResourceCostSystem.OPTIMIZE_TIER_MATERIAL
                    || ResourceCostSystem.countItem(player, ModItems.MODULE_ITEM.get()) < ResourceCostSystem.OPTIMIZE_MODULE) return 3;
        }

        // 上限 = 词条库记录的最高值（绝对值）；库中无该词条则不可优化
        Float cap = AffixLibraryCapability.get(player)
                .map(lib -> lib.get(affixKey))
                .orElse(null);
        if (cap == null) return 5;

        double current = tag.getDouble(type.key());
        float abs = (float) Math.abs(current);
        if (abs >= cap - 1e-4f) return 4; // 已达词条库记录上限

        // 每次 +1.5%
        double target = Math.min(abs + 0.015f, cap);
        double signed = current < 0 ? -target : target;
        tag.putDouble(type.key(), Math.round(signed * 1000d) / 1000d);

        // 消耗材料
        if (exotic) {
            ResourceCostSystem.tryConsumeItem(player, ModItems.MATERIALS.get(MaterialQuality.ULTIMATE).get(), ResourceCostSystem.EXOTIC_OPTIMIZE_ULTIMATE);
            ResourceCostSystem.tryConsumeItem(player, ModItems.MODULE_ITEM.get(), ResourceCostSystem.EXOTIC_OPTIMIZE_MODULE);
        } else {
            Item tierMat = ResourceCostSystem.getTierMaterial(gun);
            ResourceCostSystem.tryConsumeItem(player, tierMat, ResourceCostSystem.OPTIMIZE_TIER_MATERIAL);
            ResourceCostSystem.tryConsumeItem(player, ModItems.MODULE_ITEM.get(), ResourceCostSystem.OPTIMIZE_MODULE);
        }
        return 0;
    }

    // ===== 分解：枪销毁 → 产材料（按专精等级1-12个），不入词条库 =====

    private static int doDisassemble(ServerPlayer player) {
        ItemStack gun = player.getMainHandItem();
        boolean isGun = gun.getItem() instanceof IGun;
        boolean isAttachment = gun.getItem() instanceof IAttachment;
        if (!isGun && !isAttachment) return 1;

        // 必须有词条才能分解
        List<String> keys = AffixSystem.getAttrKeys(gun.getOrCreateTag());
        if (keys.isEmpty()) return 2;

        // 配件：给 1 个档位材料
        if (isAttachment) {
            Item tierMat = ResourceCostSystem.getTierMaterial(gun);
            gun.shrink(1);
            giveItem(player, tierMat, 1);
            player.displayClientMessage(Component.translatable("message.tacz_rpg.action.success.disassemble_attachment", 1), true);
            return 0;
        }

        // 在销毁前读取判定/材料/专精（销毁后栈为空，读取会失效）
        boolean exotic = ExoticWeaponManager.isExotic(gun);
        Item tierMat = ResourceCostSystem.getTierMaterial(gun);
        int expertiseLevel = gun.getOrCreateTag().getInt(AffixSystem.EXPERTISE_LEVEL_TAG);

        // 枪销毁
        gun.shrink(1);

        // 奇特武器：给终极材料 + 模组数据块；普通：按档位给对应材料（数量按专精）
        if (exotic) {
            int ult = ResourceCostSystem.EXOTIC_DISASSEMBLE_ULTIMATE;
            int mod = ResourceCostSystem.EXOTIC_DISASSEMBLE_MODULE;
            giveItem(player, ModItems.MATERIALS.get(MaterialQuality.ULTIMATE).get(), ult);
            giveItem(player, ModItems.MODULE_ITEM.get(), mod);
            player.displayClientMessage(Component.translatable("message.tacz_rpg.action.success.disassemble_exotic", ult, mod), true);
        } else {
            int partCount = ResourceCostSystem.getDisassembleCount(expertiseLevel);
            giveItem(player, tierMat, partCount);
            player.displayClientMessage(Component.translatable("message.tacz_rpg.action.success.disassemble", partCount), true);
        }
        return 0;
    }

    /** 发放物品到背包，放不下则掉落 */
    private static void giveItem(ServerPlayer player, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** 同步词条库到客户端 */
    private static void syncLibrary(ServerPlayer player) {
        AffixLibraryCapability.get(player).ifPresent(lib -> {
            ModNetwork.sendToPlayer(player, new SyncAffixLibraryPacket(lib.getAll()));
        });
    }
}