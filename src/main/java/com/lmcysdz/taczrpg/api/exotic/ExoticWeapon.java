package com.lmcysdz.taczrpg.api.exotic;

import com.lmcysdz.taczrpg.api.affix.AffixType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 奇特武器定义。
 *
 * <p>由数据包 {@code data/tacz_rpg/exotic_weapons/*.json} 提供，识别接口见
 * {@link ExoticWeaponManager}。奇特武器词条为固定数值，不走随机/品质档。</p>
 *
 * @param gunId        对应的 TACZ 枪械 ID（如 {@code tacz:ak47}）
 * @param displayName  奇特名称（如「撕裂者」）
 * @param exoticAffix  奇特词条（红橙色显示，如 步枪伤害）
 * @param exoticValue  奇特词条数值（MULTIPLY_BASE，0.35 = +35%）
 * @param affixes      普通词条（固定数值列表）
 */
public record ExoticWeapon(
        ResourceLocation gunId,
        String displayName,
        AffixType exoticAffix,
        double exoticValue,
        List<ExoticAffix> affixes
) {
    /** 普通词条（固定数值） */
    public record ExoticAffix(AffixType type, double value) {
    }
}
