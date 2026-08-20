package com.lmcysdz.taczrpg.api.affix;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AffixSystem {

    public static final String ATTR_KEYS_TAG = "attrKeys";
    public static final String RANK_NAME_TAG = "rankName";
    public static final String RANK_COLOR_TAG = "rankColor";
    public static final String LEVEL_TAG = "level";
    public static final String CALIBRATED_SLOT_TAG = "calibratedSlot";
    public static final String CALIBRATED_COUNT_TAG = "calibratedCount";
    public static final String EXPERTISE_LEVEL_TAG = "expertiseLevel";
    public static final String EXPERTISE_EXP_TAG = "expertiseExp";

    public static final float MAX_AFFIX_VALUE = 0.30f;

    public static final AffixRank[] RANKS = {
            new AffixRank(0, 14, 1, "§a", 0.05f, 0.10f),  // 基础 · 绿  +5%~10%
            new AffixRank(15, 29, 2, "§9", 0.10f, 0.15f), // 稀有 · 蓝  +10%~15%
            new AffixRank(30, 44, 3, "§5", 0.15f, 0.20f), // 精英 · 紫  +15%~20%
            new AffixRank(45, 60, 4, "§6", 0.20f, 0.30f)  // 传说 · 金  +20%~30%
    };

    /** 全部词条（含爆炸/近战，供属性应用遍历，兼容旧枪已有词条） */
    public static final AffixType[] AFFIX_POOL = AffixType.values();

    /** 通用随机词条池（不含 7 个类型专属伤害；专属伤害按枪型以低概率单独加入） */
    public static final AffixType[] COMMON_POOL = {
            // 红 · 伤害
            AffixType.BULLET, AffixType.HEADSHOT, AffixType.IGNORE,
            AffixType.CRIT_CHANCE, AffixType.CRIT_DAMAGE,
            AffixType.PIERCE, AffixType.BULLET_COUNT,
            // 黄 · 控制/精度/射速
            AffixType.ROUNDS, AffixType.RECOIL, AffixType.RECOIL_PITCH, AffixType.RECOIL_YAW,
            AffixType.INACCURACY, AffixType.INACCURACY_STAND, AffixType.INACCURACY_MOVE,
            AffixType.INACCURACY_SNEAK, AffixType.INACCURACY_LIE, AffixType.INACCURACY_AIM,
            AffixType.ADS_TIME, AffixType.EFFECTIVE_RANGE, AffixType.WEIGHT,
            AffixType.KNOCKBACK, AffixType.AMMO_SPEED,
            // 蓝 · 弹匣/换弹/移速等辅助
            AffixType.SPEED, AffixType.MAGAZINE_CAPACITY, AffixType.RELOAD_TIME
    };

    /** 类型专属伤害（按枪型匹配）进入随机结果的概率 */
    public static final float TYPE_DAMAGE_CHANCE = 0.30f;

    /**
     * 逐词条数值区间覆盖（按档位 [min,max]）。未覆盖的词条回退共享百分比区间（RANKS）。
     * 固定值词条（弹容/弹丸）单位是「发/颗」，其余是倍率（百分比）。
     */
    private static final Map<AffixType, float[][]> OVERRIDE_RANGES = buildOverrideRanges();

    private static Map<AffixType, float[][]> buildOverrideRanges() {
        Map<AffixType, float[][]> m = new HashMap<>();
        // 固定值词条（弹容/弹丸）：单位「发/颗」
        m.put(AffixType.MAGAZINE_CAPACITY, new float[][]{{2, 3}, {4, 5}, {6, 7}, {8, 10}});
        m.put(AffixType.BULLET_COUNT, new float[][]{{1, 1}, {1, 2}, {2, 3}, {3, 4}});
        // 连续小基数属性：抬低下限，保证最小档也明显可感（百分比）
        float[][] raised = {{0.10f, 0.15f}, {0.15f, 0.20f}, {0.20f, 0.25f}, {0.25f, 0.35f}};
        m.put(AffixType.SPEED, raised);
        m.put(AffixType.EFFECTIVE_RANGE, raised);
        // 后坐力/瞄准/初速/换弹/重量等「感受小」属性：10%~30%，削后座更明显
        float[][] recoilLike = {{0.10f, 0.15f}, {0.15f, 0.20f}, {0.20f, 0.25f}, {0.25f, 0.30f}};
        m.put(AffixType.RECOIL, recoilLike);
        m.put(AffixType.RECOIL_PITCH, recoilLike);
        m.put(AffixType.RECOIL_YAW, recoilLike);
        m.put(AffixType.ADS_TIME, recoilLike);
        m.put(AffixType.AMMO_SPEED, recoilLike);
        m.put(AffixType.RELOAD_TIME, recoilLike);
        m.put(AffixType.WEIGHT, recoilLike);
        return m;
    }

    /** 是否固定值词条（弹容/弹丸按「发/颗」计，不做百分比乘法，避免小基数被截断吃光） */
    public static boolean isFlat(AffixType type) {
        return type == AffixType.MAGAZINE_CAPACITY || type == AffixType.BULLET_COUNT;
    }

    /** 固定值词条的显示单位 lang key */
    public static String flatUnitKey(AffixType type) {
        if (type == AffixType.MAGAZINE_CAPACITY) {
            return "affix.tacz_rpg.unit.rounds";
        }
        if (type == AffixType.BULLET_COUNT) {
            return "affix.tacz_rpg.unit.pellets";
        }
        return null;
    }

    /** 词条在某档位的数值区间 [min,max]（固定值 = 发/颗，其余 = 倍率） */
    public static float[] rangeFor(AffixType type, int rankIndex) {
        float[][] override = OVERRIDE_RANGES.get(type);
        if (override != null) {
            return override[Math.max(0, Math.min(rankIndex, override.length - 1))];
        }
        AffixRank rank = RANKS[Math.max(0, Math.min(rankIndex, RANKS.length - 1))];
        return new float[]{rank.minValue(), rank.maxValue()};
    }

    /** 由 AffixRank 实例反查档位下标（0~3） */
    public static int rankIndexOf(AffixRank rank) {
        for (int i = 0; i < RANKS.length; i++) {
            if (RANKS[i] == rank) {
                return i;
            }
        }
        return 0;
    }

    /** 固定值词条数值片段：「+5 发」，不含前导空格（调用方按需加） */
    public static Component flatValueComponent(AffixType type, double value) {
        return Component.literal("+" + (int) Math.round(value) + " ")
                .append(Component.translatable(flatUnitKey(type)));
    }

    private AffixSystem() {}

    public static int getRankIndex(int agentLevel) {
        for (int i = 0; i < RANKS.length; i++) {
            if (RANKS[i].contains(agentLevel)) return i;
        }
        return 0;
    }

    public static AffixRank getRankForLevel(int agentLevel) {
        for (AffixRank rank : RANKS) {
            if (rank.contains(agentLevel)) return rank;
        }
        return RANKS[0];
    }

    public static List<AffixType> rollAffixes(AffixRank rank, RandomSource random, CompoundTag target, AffixType typeDamage, int maxCount) {
        if (rank.affixCount() <= 0) return Collections.emptyList();
        List<AffixType> selected = new ArrayList<>();
        int count = Math.min(rank.affixCount(), maxCount);

        // 30% 概率加入本枪的类型专属伤害（不污染其他枪型的池子）
        if (typeDamage != null && random.nextFloat() < TYPE_DAMAGE_CHANCE) {
            selected.add(typeDamage);
        }

        // 从通用池随机补足剩余名额
        int remaining = count - selected.size();
        if (remaining > 0) {
            List<AffixType> pool = new ArrayList<>(List.of(COMMON_POOL));
            Collections.shuffle(pool, new java.util.Random(random.nextLong()));
            for (int i = 0; i < remaining && i < pool.size(); i++) {
                selected.add(pool.get(i));
            }
        }

        // 写 NBT
        for (AffixType type : selected) {
            float value = rollValue(rank, random, type);
            if (type.isNegative()) value = -value;
            double stored = isFlat(type) ? (double) Math.round(value) : round3(value);
            target.putDouble(type.key(), stored);
        }
        return selected;
    }

    public static float rollValue(AffixRank rank, RandomSource random, AffixType type) {
        float[] range = rangeFor(type, rankIndexOf(rank));
        return range[0] + random.nextFloat() * (range[1] - range[0]);
    }

    public static List<String> getAttrKeys(CompoundTag tag) {
        List<String> keys = new ArrayList<>();
        if (tag.contains(ATTR_KEYS_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(ATTR_KEYS_TAG, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                keys.add(list.getString(i));
            }
        }
        return keys;
    }

    public static void setAttrKeys(CompoundTag tag, List<AffixType> affixes) {
        ListTag list = new ListTag();
        for (AffixType type : affixes) {
            list.add(StringTag.valueOf(type.key()));
        }
        tag.put(ATTR_KEYS_TAG, list);
    }

    public static void clearAffixes(CompoundTag tag) {
        for (String key : getAttrKeys(tag)) {
            tag.remove(key);
        }
        tag.remove(ATTR_KEYS_TAG);
    }

    public static boolean isGun(ItemStack stack) {
        return stack.getItem() instanceof com.tacz.guns.api.item.IGun;
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}