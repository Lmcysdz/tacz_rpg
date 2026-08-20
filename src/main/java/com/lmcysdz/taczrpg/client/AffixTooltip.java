package com.lmcysdz.taczrpg.client;

import com.lmcysdz.taczrpg.api.affix.AffixOperation;
import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeapon;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import com.lmcysdz.taczrpg.capability.AgentLevelCapability;
import com.lmcysdz.taczrpg.config.TaczRpgConfig;
import com.tacz.guns.api.item.IAttachment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 客户端 Tooltip：显示已登记枪械/配件的品质与词条。
 */
@OnlyIn(Dist.CLIENT)
public class AffixTooltip {

    private static final int EXOTIC_RGB = 0xFF6B4A; // 奇特武器红橙色

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(AffixSystem.LEVEL_TAG, Tag.TAG_INT)) {
            return;
        }
        // 配件词条开关：关闭则配件不显示词条（config 默认开启，整合包可关）
        if (stack.getItem() instanceof IAttachment && !TaczRpgConfig.ENABLE_ATTACHMENT_AFFIXES.get()) {
            return;
        }

        List<Component> tooltip = event.getToolTip();

        // 移除原版「在主手时:」属性修饰器块（来源见 removeVanillaModifierTooltip），只保留我们的词条展示
        removeVanillaModifierTooltip(stack, tooltip);

        // 我们的词条行始终显示
        ExoticWeapon exotic = ExoticWeaponManager.getExotic(stack);
        if (exotic != null) {
            tooltip.add(expertiseLine(stack));
            addAgentProgressLine(tooltip);
            addAffixLines(tooltip, tag, exotic);
            return;
        }

        int level = tag.getInt(AffixSystem.LEVEL_TAG);
        String color = tag.getString(AffixSystem.RANK_COLOR_TAG);
        ChatFormatting fmt = parseColor(color);
        MutableComponent header = Component.literal("◆ ")
                .append(Component.translatable("tooltip.tacz_rpg.agent_level"))
                .append(Component.literal(" Lv." + level));
        if (fmt != null) {
            header = header.withStyle(fmt);
        }
        tooltip.add(header);
        tooltip.add(expertiseLine(stack));
        addAgentProgressLine(tooltip);
        addAffixLines(tooltip, tag, null);
    }

    /** 玩家实时特工等级进度行（需要客户端已同步数据，Lv.0 且 0 经验时不显示） */
    private static void addAgentProgressLine(List<Component> tooltip) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        AgentLevelCapability.get(player).ifPresent(agent -> {
            int al = agent.getLevel();
            int ae = agent.getExp();
            if (al <= 0 && ae <= 0) {
                return;
            }
            int at = agent.getExpToNextLevel();
            if (at <= 0) {
                return; // 已满级，无需进度
            }
            tooltip.add(Component.translatable("tooltip.tacz_rpg.agent_progress", al, ae, at)
                    .withStyle(ChatFormatting.DARK_GRAY));
        });
    }

    /**
     * 移除原版渲染的属性修饰器 Tooltip 段（「在主手时: …」）。
     *
     * <p>{@code AffixAttributeHandler} 通过 {@code ItemAttributeModifierEvent} 给枪械挂 TAA 属性修饰器，
     * 原版会把它们原样渲染进 Tooltip（TAA 属性名 + 全精度数值），与我们的词条行（取整百分比）重复。
     * 按「标题行 + 修饰器数量行」精确移除；此块完全来自本模组挂载的修饰器，不会误伤其他信息。</p>
     */
    private static void removeVanillaModifierTooltip(ItemStack stack, List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals("item.modifiers.mainhand")) {
                int count = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).size();
                tooltip.subList(i, Math.min(tooltip.size(), i + 1 + count)).clear();
                return;
            }
        }
    }

    /** 专精等级行：显示当前枪的专精进度与固定伤害加成（每级 +2） */
    private static Component expertiseLine(ItemStack stack) {
        int lv = AffixOperation.getExpertiseLevel(stack);
        return Component.translatable("tooltip.tacz_rpg.expertise_line", lv, AffixOperation.MAX_EXPERTISE_LEVEL,
                lv * AffixOperation.EXPERTISE_DAMAGE_PER_LEVEL)
                .withStyle(ChatFormatting.AQUA);
    }

    private static void addAffixLines(List<Component> tooltip, CompoundTag tag, @Nullable ExoticWeapon exotic) {
        List<String> keys = AffixSystem.getAttrKeys(tag);
        for (String key : keys) {
            AffixType affix = AffixType.byKey(key);
            if (affix == null || !tag.contains(key, Tag.TAG_DOUBLE)) {
                continue;
            }
            double value = tag.getDouble(key);
            if (value == 0d) {
                continue;
            }
            // 固定值词条（弹容/弹丸）显示「+N 发/+N 颗」；百分比维持「增加/减少 ±%」
            Component valueSuffix;
            if (AffixSystem.isFlat(affix)) {
                valueSuffix = Component.literal(": ").append(AffixSystem.flatValueComponent(affix, value));
            } else {
                int percent = (int) Math.round(Math.abs(value) * 100);
                Component prefix = Component.translatable(value < 0
                        ? "tooltip.tacz_rpg.prefix.decrease" : "tooltip.tacz_rpg.prefix.increase");
                valueSuffix = Component.literal(": ").append(prefix).append(Component.literal(percent + "%"));
            }
            boolean isExoticAffix = exotic != null && key.equals(exotic.exoticAffix().key());
            if (isExoticAffix) {
                tooltip.add(Component.translatable("tooltip.tacz_rpg.exotic_prefix")
                        .append(Component.translatable(affix.translationKey()))
                        .append(valueSuffix)
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(EXOTIC_RGB))));
            } else {
                tooltip.add(Component.literal("◈ ")
                        .append(Component.translatable(affix.translationKey()))
                        .append(valueSuffix)
                        .withStyle(affix.category().chatColor()));
            }
        }
    }

    private static ChatFormatting parseColor(String color) {
        if (color == null || color.length() < 2) {
            return null;
        }
        return ChatFormatting.getByCode(color.charAt(1));
    }
}
