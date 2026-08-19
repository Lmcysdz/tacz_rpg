package com.lmcysdz.taczrpg.client;

import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeapon;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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

        List<Component> tooltip = event.getToolTip();

        // 奇特武器：名字已由 setHoverName 显示「奇特 <displayName>」，这里只补词条行
        ExoticWeapon exotic = ExoticWeaponManager.getExotic(stack);
        if (exotic != null) {
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
        addAffixLines(tooltip, tag, null);
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
            int percent = (int) Math.round(Math.abs(value) * 100);
            String prefix = value < 0 ? "减少" : "增加";
            boolean isExoticAffix = exotic != null && key.equals(exotic.exoticAffix().key());
            if (isExoticAffix) {
                tooltip.add(Component.literal("奇特词条：")
                        .append(Component.translatable(affix.translationKey()))
                        .append(Component.literal(": " + prefix + percent + "%"))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(EXOTIC_RGB))));
            } else {
                tooltip.add(Component.literal("◈ ")
                        .append(Component.translatable(affix.translationKey()))
                        .append(Component.literal(": " + prefix + percent + "%"))
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
