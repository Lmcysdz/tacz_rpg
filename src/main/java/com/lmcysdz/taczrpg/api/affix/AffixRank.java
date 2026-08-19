package com.lmcysdz.taczrpg.api.affix;

/**
 * 品质档：由特工等级区间决定词条数与数值范围。
 *
 * <p>对应开发文档「等级阈值与词条」表。</p>
 *
 * @param minLevel  该档的最低特工等级（含）
 * @param maxLevel  该档的最高特工等级（含，{@link Integer#MAX_VALUE} 表示无上限）
 * @param affixCount 可抽取的词条数量
 * @param color     品质颜色（Minecraft § 颜色码，如 §a / §9 / §5）
 * @param minValue  数值区间下限（倍率，0.02 = +2%）
 * @param maxValue  数值区间上限（倍率）
 */
public record AffixRank(int minLevel, int maxLevel, int affixCount, String color, float minValue, float maxValue) {

    public boolean contains(int agentLevel) {
        return agentLevel >= minLevel && agentLevel <= maxLevel;
    }
}
