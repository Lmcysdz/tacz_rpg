package com.lmcysdz.taczrpg.api.affix;

import com.lmcysdz.taczrpg.item.AffixExtractItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 词条操作核心：校准 / 优化 / 专精 的 NBT 层实现。
 *
 * <p><b>校准</b>：消耗词条提取物，替换目标枪对应词条位。每件枪只能校准一次，
 * 校准后锁定该词条位，后续只能改该位。</p>
 * <p><b>优化</b>：消耗材料，将某词条数值向品质档上限提升。校准过的词条不可优化。</p>
 * <p><b>专精</b>：击杀积累经验，升级加伤害（属性挂载见 {@link com.lmcysdz.taczrpg.event.AffixAttributeHandler}）。</p>
 */
public final class AffixOperation {

    private AffixOperation() {
    }

    /* ==================== 校准 ==================== */

    /**
     * 用词条提取物对目标枪械执行校准。
     *
     * @return 结果码：0 成功，1 目标非枪/无词条，2 该枪已校准且提取物词条位与锁定位不符
     */
    public static int calibrate(ItemStack gun, ItemStack extract) {
        if (!AffixSystem.isGun(gun)) {
            return 1;
        }
        AffixType extractType = AffixExtractItem.getAffixType(extract);
        if (extractType == null) {
            return 1;
        }
        double extractValue = AffixExtractItem.getAffixValue(extract);
        CompoundTag gunTag = gun.getOrCreateTag();

        List<String> attrKeys = AffixSystem.getAttrKeys(gunTag);
        int extractIndex = attrKeys.indexOf(extractType.key());
        int calibratedSlot = gunTag.getInt(AffixSystem.CALIBRATED_SLOT_TAG);

        // 若提取词条不在目标枪上，允许直接"添加"该词条（全境封锁校准也允许替换为库中词条）
        if (extractIndex == -1) {
            if (calibratedSlot != -1) {
                return 2;
            }
            gunTag.putDouble(extractType.key(), extractValue);
            attrKeys.add(extractType.key());
            AffixSystem.setAttrKeys(gunTag, attrKeys.stream().map(AffixType::byKey).filter(java.util.Objects::nonNull).toList());
            gunTag.putInt(AffixSystem.CALIBRATED_SLOT_TAG, attrKeys.size() - 1);
            return 0;
        }

        // 已锁定且锁定位不是提取物词条位 → 拒绝
        if (calibratedSlot != -1 && calibratedSlot != extractIndex) {
            return 2;
        }

        gunTag.putDouble(extractType.key(), extractValue);
        gunTag.putInt(AffixSystem.CALIBRATED_SLOT_TAG, extractIndex);
        return 0;
    }

    /* ==================== 优化 ==================== */

    /**
     * 对目标枪械的指定词条执行一次优化（数值向品质档上限提升）。
     *
     * @return 结果码：0 成功，1 无该词条，2 该词条已校准不可优化，3 已达满值
     */
    public static int optimize(ItemStack gun, AffixType type) {
        if (!AffixSystem.isGun(gun)) {
            return 1;
        }
        CompoundTag gunTag = gun.getOrCreateTag();
        if (!gunTag.contains(type.key(), Tag.TAG_DOUBLE)) {
            return 1;
        }

        List<String> attrKeys = AffixSystem.getAttrKeys(gunTag);
        int index = attrKeys.indexOf(type.key());
        int calibratedSlot = gunTag.getInt(AffixSystem.CALIBRATED_SLOT_TAG);
        if (calibratedSlot == index) {
            return 2;
        }

        double current = gunTag.getDouble(type.key());
        int ri = AffixSystem.getRankIndex(gunTag.getInt(AffixSystem.LEVEL_TAG));
        float[] range = AffixSystem.rangeFor(type, ri);
        double maxValue = range[1];
        double minValue = range[0];

        double abs = Math.abs(current);
        if (abs >= maxValue) {
            return 3;
        }
        // 固定值词条（弹容/弹丸）：每次优化 +1 发/颗（整数）
        if (AffixSystem.isFlat(type)) {
            gunTag.putDouble(type.key(), (double) Math.round(abs + 1.0));
            return 0;
        }
        // 每次优化提升一格（10 步从 min 到 max）
        double step = (maxValue - minValue) / 10.0;
        double target = Math.min(abs + step, maxValue);
        double signed = current < 0 ? -target : target;
        gunTag.putDouble(type.key(), Math.round(signed * 1000d) / 1000d);
        return 0;
    }

    /* ==================== 专精 ==================== */

    /** 单次击杀获得的专精经验 */
    public static final int EXP_PER_KILL = 1;
    /** 每级专精所需的经验（20 杀/级，击杀任意生物） */
    public static final int EXP_TO_LEVEL = 20;
    /** 专精上限 */
    public static final int MAX_EXPERTISE_LEVEL = 30;
    /** 每级专精固定伤害（每级 +2，上限 +60，见 ExpertiseHandler 的固定伤害应用） */
    public static final int EXPERTISE_DAMAGE_PER_LEVEL = 2;

    /**
     * 给枪械增加专精经验，满则升级。
     *
     * @return 是否升级
     */
    public static boolean addExpertiseExp(ItemStack gun, int amount) {
        if (!AffixSystem.isGun(gun)) {
            return false;
        }
        CompoundTag tag = gun.getOrCreateTag();
        int level = tag.getInt(AffixSystem.EXPERTISE_LEVEL_TAG);
        int exp = tag.getInt(AffixSystem.EXPERTISE_EXP_TAG);
        if (level >= MAX_EXPERTISE_LEVEL) {
            return false;
        }
        exp += amount;
        boolean leveledUp = false;
        while (level < MAX_EXPERTISE_LEVEL && exp >= EXP_TO_LEVEL) {
            exp -= EXP_TO_LEVEL;
            level++;
            leveledUp = true;
        }
        tag.putInt(AffixSystem.EXPERTISE_LEVEL_TAG, level);
        tag.putInt(AffixSystem.EXPERTISE_EXP_TAG, exp);
        return leveledUp;
    }

    /** 读取专精等级 */
    public static int getExpertiseLevel(ItemStack gun) {
        if (!AffixSystem.isGun(gun)) {
            return 0;
        }
        return gun.getOrCreateTag().getInt(AffixSystem.EXPERTISE_LEVEL_TAG);
    }
}
