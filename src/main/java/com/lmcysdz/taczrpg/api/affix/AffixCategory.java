package com.lmcysdz.taczrpg.api.affix;

import net.minecraft.ChatFormatting;

/**
 * 词条类别：决定词条在 Tooltip / GUI 中的配色与语义分组。
 *
 * <ul>
 *   <li>{@link #DAMAGE} 伤害 —— 红色</li>
 *   <li>{@link #CONTROL} 控制 / 精度 —— 黄色</li>
 *   <li>{@link #OTHER} 其他（机动等）—— 蓝色</li>
 * </ul>
 *
 * <p>预留 {@link #SKILL} / {@link #DEFENSE}，供后续联动其他模组实现真正的技能 / 防御词条。</p>
 */
public enum AffixCategory {
    /** 伤害相关 —— 红 */
    DAMAGE(0xFFE0554B, ChatFormatting.RED, "category.tacz_rpg.damage"),
    /** 控制 / 精度相关 —— 黄 */
    CONTROL(0xFFF2C14E, ChatFormatting.YELLOW, "category.tacz_rpg.control"),
    /** 其他（机动等）—— 蓝 */
    OTHER(0xFF4E9BE0, ChatFormatting.BLUE, "category.tacz_rpg.other"),
    /** 技能（预留） */
    SKILL(0xFF2ECC71, ChatFormatting.GREEN, "category.tacz_rpg.skill"),
    /** 防御（预留） */
    DEFENSE(0xFFE0C968, ChatFormatting.GOLD, "category.tacz_rpg.defense");

    /** GUI 渲染用的 ARGB 颜色（全息风格，高饱和） */
    private final int color;
    /** Tooltip 用的 MC 格式颜色 */
    private final ChatFormatting chatColor;
    /** 类别显示名翻译键 */
    private final String translationKey;

    AffixCategory(int color, ChatFormatting chatColor, String translationKey) {
        this.color = color;
        this.chatColor = chatColor;
        this.translationKey = translationKey;
    }

    public int color() {
        return color;
    }

    public ChatFormatting chatColor() {
        return chatColor;
    }

    public String translationKey() {
        return translationKey;
    }
}
