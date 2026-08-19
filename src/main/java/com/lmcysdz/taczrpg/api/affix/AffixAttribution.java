package com.lmcysdz.taczrpg.api.affix;

import com.lmcysdz.taczrpg.api.exotic.ExoticWeapon;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import com.lmcysdz.taczrpg.api.resource.GunTypeUtil;
import com.tacz.guns.api.item.IAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 词条登记：根据特工等级给枪械/配件生成品质与词条，写入 NBT。
 */
public final class AffixAttribution {

    private AffixAttribution() {
    }

    /** 是否已登记（有 level 标签即视为已登记） */
    public static boolean isRegistered(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(AffixSystem.LEVEL_TAG, Tag.TAG_INT);
    }

    /**
     * 给物品登记词条（幂等：已登记则跳过）。
     *
     * @param agentLevel 特工等级，决定品质档与词条数
     * @return 是否执行了登记
     */
    public static boolean applyAffixes(ItemStack stack, int agentLevel, RandomSource random) {
        if (isRegistered(stack)) {
            return false;
        }
        // 奇特武器：按数据包定义应用固定词条
        ExoticWeapon exotic = ExoticWeaponManager.getExotic(stack);
        if (exotic != null) {
            return applyExoticAffixes(stack, exotic, agentLevel);
        }
        AffixRank rank = AffixSystem.getRankForLevel(agentLevel);
        CompoundTag tag = stack.getOrCreateTag();

        AffixSystem.clearAffixes(tag);
        // 本枪类型专属伤害（无匹配枪型则为 null；配件无枪型）
        AffixType typeDamage = GunTypeUtil.getTypeDamage(stack);
        // 配件词条最多 3 条（随等级升满）
        boolean isAttachment = stack.getItem() instanceof IAttachment;
        List<AffixType> affixes = AffixSystem.rollAffixes(rank, random, tag, typeDamage, isAttachment ? 3 : Integer.MAX_VALUE);

        tag.putString(AffixSystem.RANK_COLOR_TAG, rank.color());
        tag.putInt(AffixSystem.LEVEL_TAG, agentLevel);
        AffixSystem.setAttrKeys(tag, affixes);

        // 初始化校准/专精字段
        if (!tag.contains(AffixSystem.CALIBRATED_SLOT_TAG, Tag.TAG_INT)) {
            tag.putInt(AffixSystem.CALIBRATED_SLOT_TAG, -1);
        }
        if (!tag.contains(AffixSystem.EXPERTISE_LEVEL_TAG, Tag.TAG_INT)) {
            tag.putInt(AffixSystem.EXPERTISE_LEVEL_TAG, 0);
        }
        if (!tag.contains(AffixSystem.EXPERTISE_EXP_TAG, Tag.TAG_INT)) {
            tag.putInt(AffixSystem.EXPERTISE_EXP_TAG, 0);
        }
        return true;
    }

    /**
     * 奇特武器：设置红橙名字 + 写入固定词条（奇特词条 + 普通词条），不走随机/品质档。
     * 公开供创造标签页构建奇特武器使用。
     */
    public static boolean applyExoticAffixes(ItemStack stack, ExoticWeapon exotic, int agentLevel) {
        stack.setHoverName(Component.literal("奇特 " + exotic.displayName())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ExoticWeaponManager.EXOTIC_RGB))));
        CompoundTag tag = stack.getOrCreateTag();
        AffixSystem.clearAffixes(tag);
        List<AffixType> keys = new ArrayList<>();
        tag.putDouble(exotic.exoticAffix().key(), exotic.exoticValue());
        keys.add(exotic.exoticAffix());
        for (ExoticWeapon.ExoticAffix a : exotic.affixes()) {
            tag.putDouble(a.type().key(), a.value());
            keys.add(a.type());
        }
        AffixSystem.setAttrKeys(tag, keys);
        tag.putInt(AffixSystem.LEVEL_TAG, agentLevel);
        tag.putInt(AffixSystem.CALIBRATED_SLOT_TAG, -1);
        tag.putInt(AffixSystem.CALIBRATED_COUNT_TAG, 0);
        tag.putInt(AffixSystem.EXPERTISE_LEVEL_TAG, 0);
        tag.putInt(AffixSystem.EXPERTISE_EXP_TAG, 0);
        return true;
    }
}
