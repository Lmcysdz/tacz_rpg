package com.lmcysdz.taczrpg.api.affix;

import net.minecraft.resources.ResourceLocation;

/**
 * 词条类型：枪械/配件上的随机属性，映射到一个 TAA 属性。
 *
 * <p>与 KubeJS 原型 {@code ATTR_POOL} 一一对应。</p>
 */
public enum AffixType {
    // ===== 红色 · 伤害 =====
    /** 额外子弹伤害 */
    BULLET("bullet", "taa:bullet_gundamage", false, AffixCategory.DAMAGE),
    /** 爆头伤害加成 */
    HEADSHOT("headshot", "taa:headshot_multiplier", false, AffixCategory.DAMAGE),
    /** 护甲穿透 */
    IGNORE("ignore", "taa:armor_ignore", false, AffixCategory.DAMAGE),
    /** 暴击几率（自定义属性，base 1.0） */
    CRIT_CHANCE("crit_chance", "tacz_rpg:crit_chance", false, AffixCategory.DAMAGE),
    /** 暴击伤害（自定义属性，base 1.0） */
    CRIT_DAMAGE("crit_damage", "tacz_rpg:crit_damage", false, AffixCategory.DAMAGE),
    /** 穿透实体 */
    PIERCE("pierce", "taa:pierce", false, AffixCategory.DAMAGE),
    /** 弹头数量 */
    BULLET_COUNT("bullet_count", "taa:bullet_count", false, AffixCategory.DAMAGE),
    /** 手枪伤害 */
    BULLET_PISTOL("bullet_pistol", "taa:bullet_gundamage_pistol", false, AffixCategory.DAMAGE),
    /** 步枪伤害 */
    BULLET_RIFLE("bullet_rifle", "taa:bullet_gundamage_rifle", false, AffixCategory.DAMAGE),
    /** 霰弹枪伤害 */
    BULLET_SHOTGUN("bullet_shotgun", "taa:bullet_gundamage_shotgun", false, AffixCategory.DAMAGE),
    /** 狙击枪伤害 */
    BULLET_SNIPER("bullet_sniper", "taa:bullet_gundamage_sniper", false, AffixCategory.DAMAGE),
    /** 冲锋枪伤害 */
    BULLET_SMG("bullet_smg", "taa:bullet_gundamage_smg", false, AffixCategory.DAMAGE),
    /** 轻机枪伤害 */
    BULLET_LMG("bullet_lmg", "taa:bullet_gundamage_lmg", false, AffixCategory.DAMAGE),
    /** 发射器伤害 */
    BULLET_LAUNCHER("bullet_launcher", "taa:bullet_gundamage_launcher", false, AffixCategory.DAMAGE),
    /** 近战伤害（注册但暂不进随机池） */
    MELEE_DAMAGE("melee_damage", "taa:melee_damage", false, AffixCategory.DAMAGE),
    /** 爆炸伤害（注册但暂不进随机池） */
    EXPLODE_DAMAGE("explode_damage", "taa:explosion_damage", false, AffixCategory.DAMAGE),

    // ===== 黄色 · 控制 / 精度 / 射速 =====
    /** 射击速度 */
    ROUNDS("rounds", "taa:rounds_per_minute", false, AffixCategory.CONTROL),
    /** 后坐力（负值 = 减少后坐力） */
    RECOIL("recoil", "taa:recoil", true, AffixCategory.CONTROL),
    /** 垂直后坐力 */
    RECOIL_PITCH("recoil_pitch", "taa:recoil_pitch", true, AffixCategory.CONTROL),
    /** 水平后坐力 */
    RECOIL_YAW("recoil_yaw", "taa:recoil_yaw", true, AffixCategory.CONTROL),
    /** 射击散布（负值 = 更准） */
    INACCURACY("inaccuracy", "taa:inaccuracy", true, AffixCategory.CONTROL),
    /** 站立散布 */
    INACCURACY_STAND("inaccuracy_stand", "taa:inaccuracy_stand", true, AffixCategory.CONTROL),
    /** 移动散布 */
    INACCURACY_MOVE("inaccuracy_move", "taa:inaccuracy_move", true, AffixCategory.CONTROL),
    /** 潜行散布 */
    INACCURACY_SNEAK("inaccuracy_sneak", "taa:inaccuracy_sneak", true, AffixCategory.CONTROL),
    /** 趴下散布 */
    INACCURACY_LIE("inaccuracy_lie", "taa:inaccuracy_lie", true, AffixCategory.CONTROL),
    /** 瞄准散布 */
    INACCURACY_AIM("inaccuracy_aim", "taa:inaccuracy_aim", true, AffixCategory.CONTROL),
    /** 瞄准时间（负值 = 更快开镜） */
    ADS_TIME("ads_time", "taa:ads_time", true, AffixCategory.CONTROL),
    /** 有效射程 */
    EFFECTIVE_RANGE("effective_range", "taa:effective_range", false, AffixCategory.CONTROL),
    /** 枪械重量（负值 = 更轻） */
    WEIGHT("weight", "taa:weight", true, AffixCategory.CONTROL),
    /** 击退力度 */
    KNOCKBACK("knockback", "taa:knockback", false, AffixCategory.CONTROL),
    /** 子弹初速 */
    AMMO_SPEED("ammo_speed", "taa:ammo_speed", false, AffixCategory.CONTROL),
    /** 爆炸击退（注册但暂不进随机池） */
    EXPLODE_KNOCKBACK("explode_knockback", "taa:explosion_knockbacknew", false, AffixCategory.CONTROL),

    // ===== 蓝色 · 弹匣 / 换弹 / 移速等辅助 =====
    /** 额外移速 */
    SPEED("speed", "taa:move_speed", false, AffixCategory.OTHER),
    /** 弹匣容量 */
    MAGAZINE_CAPACITY("magazine_capacity", "taa:magazine_capacity", false, AffixCategory.OTHER),
    /** 换弹速度（负值 = 更快换弹） */
    RELOAD_TIME("reload_time", "taa:reload_time", true, AffixCategory.OTHER),
    /** 近战距离（注册但暂不进随机池） */
    MELEE_DISTANCE("melee_distance", "taa:melee_distance", false, AffixCategory.OTHER),
    /** 爆炸范围（注册但暂不进随机池） */
    EXPLODE_RADIUS("explode_radius", "taa:explosion_radius", false, AffixCategory.OTHER),
    /** 爆炸延迟（注册但暂不进随机池） */
    EXPLODE_DELAY("explode_delay", "taa:explosion_delay", false, AffixCategory.OTHER),
    /** 爆炸破坏方块（注册但暂不进随机池） */
    EXPLODE_DESTROY("explode_destroy", "taa:explosion_destroy_blocknew", false, AffixCategory.OTHER);

    /** NBT 中的键名（与原型一致） */
    private final String key;
    /** 对应的 TAA 属性 ID */
    private final ResourceLocation attributeId;
    /** 是否为反向属性（数值取负，如后坐力） */
    private final boolean negative;
    /** 词条类别（决定配色） */
    private final AffixCategory category;

    AffixType(String key, String attributeId, boolean negative, AffixCategory category) {
        this.key = key;
        this.attributeId = new ResourceLocation(attributeId);
        this.negative = negative;
        this.category = category;
    }

    public String key() {
        return key;
    }

    public ResourceLocation attributeId() {
        return attributeId;
    }

    /** 是否反向属性：最终数值应取负 */
    public boolean isNegative() {
        return negative;
    }

    /** 词条类别（配色 + 语义分组） */
    public AffixCategory category() {
        return category;
    }

    /** 翻译键，用于 Tooltip / GUI 显示 */
    public String translationKey() {
        return "affix.tacz_rpg." + key;
    }

    /** 按 NBT 键名反查词条类型，未匹配返回 null */
    public static AffixType byKey(String key) {
        for (AffixType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }
}
