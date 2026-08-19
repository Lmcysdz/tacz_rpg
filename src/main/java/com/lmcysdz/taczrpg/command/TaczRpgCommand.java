package com.lmcysdz.taczrpg.command;

import com.lmcysdz.taczrpg.api.affix.AffixOperation;
import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.lmcysdz.taczrpg.capability.AgentLevel;
import com.lmcysdz.taczrpg.capability.AgentLevelCapability;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * /taczrpg setlevel <等级> —— 设置玩家特工等级（调试用）。
 */
public class TaczRpgCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("taczrpg")
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 60))
                                        .executes(ctx -> {
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            CommandSourceStack src = ctx.getSource();
                                            ServerPlayer player = src.getPlayerOrException();
                                            return setAgentLevel(player, level);
                                        })
                                )
                        )
                        .then(Commands.literal("getlevel")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerPlayer player = src.getPlayerOrException();
                                    return getAgentLevel(player);
                                })
                        )
                        .then(Commands.literal("setexpertise")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, AffixOperation.MAX_EXPERTISE_LEVEL))
                                        .executes(ctx -> {
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            return setExpertise(player, level);
                                        })
                                )
                        )
                        .then(Commands.literal("getexpertise")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    return getExpertise(player);
                                })
                        )
                        .then(Commands.literal("getattr")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    return getAttr(player);
                                })
                        )
        );
    }

    private static int setAgentLevel(ServerPlayer player, int level) {
        player.getPersistentData().putInt("tacz_rpg_agent_level", level);
        // 通过能力接口设置
        AgentLevelCapability.get(player).ifPresent(al -> {
            if (al instanceof com.lmcysdz.taczrpg.capability.AgentLevelImpl impl) {
                impl.setLevel(level);
                impl.setExp(0);
            }
        });
        player.displayClientMessage(Component.translatable("message.tacz_rpg.command.level_set", level), true);
        return 1;
    }

    private static int getAgentLevel(ServerPlayer player) {
        int level = AgentLevelCapability.get(player).map(AgentLevel::getLevel).orElse(0);
        player.displayClientMessage(Component.translatable("message.tacz_rpg.command.level_current", level), true);
        return 1;
    }

    /** 设置当前手持枪械的专精等级（调试用） */
    private static int setExpertise(ServerPlayer player, int level) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof IGun)) {
            player.displayClientMessage(Component.literal("§c请手持一把枪械"), true);
            return 0;
        }
        CompoundTag tag = held.getOrCreateTag();
        tag.putInt(AffixSystem.EXPERTISE_LEVEL_TAG, level);
        tag.putInt(AffixSystem.EXPERTISE_EXP_TAG, 0);
        player.displayClientMessage(Component.literal("§a已设置当前枪械专精等级为 " + level), true);
        return 1;
    }

    /** 查询当前手持枪械的专精等级与经验 */
    private static int getExpertise(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof IGun)) {
            player.displayClientMessage(Component.literal("§c请手持一把枪械"), true);
            return 0;
        }
        int lv = AffixOperation.getExpertiseLevel(held);
        int exp = held.getOrCreateTag().getInt(AffixSystem.EXPERTISE_EXP_TAG);
        player.displayClientMessage(Component.literal("§7当前枪械专精: " + lv + "/" + AffixOperation.MAX_EXPERTISE_LEVEL + "，经验 " + exp + "/" + AffixOperation.EXP_TO_LEVEL), true);
        return 1;
    }

    /**
     * 调试：打印当前手持枪械的词条 → TAA 属性实际值，并检查配件槽位 NBT 是否带词条。
     * 空手 / 无词条时属性 = 1.0；带词条时应为 1.0 + 词条值（如 +5% → 1.05）。
     */
    private static int getAttr(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof IGun iGun)) {
            msg(player, "§7请手持一把枪械（空手时 TAA 属性=基准 1.0）");
            return 0;
        }
        // 1) 枪本体词条 → 属性值
        msg(player, "§b==== 枪械本体词条 ====");
        List<String> keys = AffixSystem.getAttrKeys(held.getOrCreateTag());
        if (keys.isEmpty()) {
            msg(player, "§7  无词条（属性=1.0）");
        }
        for (String k : keys) {
            AffixType t = AffixType.byKey(k);
            if (t == null) continue;
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(t.attributeId());
            if (attr == null) continue;
            msg(player, "§7  " + t.key() + " → " + t.attributeId() + " = " + (float) player.getAttributeValue(attr));
        }
        // 2) 配件槽位 NBT（getAttachmentTag 已直接返回配件词条 NBT）
        msg(player, "§b==== 配件槽位 ====");
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) continue;
            CompoundTag tag = iGun.getAttachmentTag(held, type);
            if (tag == null) continue; // 该槽无配件
            String attachmentId = tag.getString("AttachmentId");
            List<String> affixKeys = new java.util.ArrayList<>();
            for (AffixType t : AffixType.values()) {
                if (tag.contains(t.key(), Tag.TAG_DOUBLE)) affixKeys.add(t.key());
            }
            String status;
            if (attachmentId.isEmpty()) {
                status = "§7空槽?";
            } else if (affixKeys.isEmpty()) {
                status = "§e已装但无词条（配件可能未登记）";
            } else {
                status = "§a词条: " + String.join(",", affixKeys);
            }
            msg(player, "§7  " + type + " [" + attachmentId + "] " + status);
            for (String k : affixKeys) {
                AffixType t = AffixType.byKey(k);
                if (t == null) continue;
                Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(t.attributeId());
                if (attr == null) continue;
                msg(player, "§7    " + k + " → " + t.attributeId() + " = " + (float) player.getAttributeValue(attr));
            }
        }
        return 1;
    }

    /** 聊天输出（可滚动），便于调试查看多行 */
    private static void msg(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}