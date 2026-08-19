package com.lmcysdz.taczrpg.api.affix;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            float value = rollValue(rank, random);
            if (type.isNegative()) value = -value;
            target.putDouble(type.key(), round3(value));
        }
        return selected;
    }

    public static float rollValue(AffixRank rank, RandomSource random) {
        return rank.minValue() + random.nextFloat() * (rank.maxValue() - rank.minValue());
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