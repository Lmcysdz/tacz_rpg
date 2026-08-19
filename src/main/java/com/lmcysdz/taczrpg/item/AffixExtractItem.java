package com.lmcysdz.taczrpg.item;

import com.lmcysdz.taczrpg.api.affix.AffixType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 词条提取物：校准功能的载体，NBT 存一个被提取的词条（类型 + 数值）。
 */
public class AffixExtractItem extends Item {

    public static final String AFFIX_KEY_TAG = "AffixKey";
    public static final String AFFIX_VALUE_TAG = "AffixValue";

    public AffixExtractItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** 创建一个承载指定词条的提取物 */
    public static ItemStack create(AffixType type, float value) {
        ItemStack stack = new ItemStack(com.lmcysdz.taczrpg.registry.ModItems.AFFIX_EXTRACT.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(AFFIX_KEY_TAG, type.key());
        tag.putDouble(AFFIX_VALUE_TAG, value);
        return stack;
    }

    /** 读取提取物承载的词条类型，无则 null */
    @Nullable
    public static AffixType getAffixType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(AFFIX_KEY_TAG, Tag.TAG_STRING)) {
            return AffixType.byKey(tag.getString(AFFIX_KEY_TAG));
        }
        return null;
    }

    /** 读取提取物承载的词条数值 */
    public static double getAffixValue(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(AFFIX_VALUE_TAG, Tag.TAG_DOUBLE)) {
            return tag.getDouble(AFFIX_VALUE_TAG);
        }
        return 0d;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        AffixType type = getAffixType(stack);
        if (type != null) {
            double value = getAffixValue(stack);
            String sign = value < 0 ? "减少" : "增加";
            int percent = (int) Math.round(Math.abs(value) * 100);
            tooltip.add(Component.translatable(type.translationKey()).withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": " + sign + percent + "%").withStyle(ChatFormatting.GRAY)));
        }
    }
}
