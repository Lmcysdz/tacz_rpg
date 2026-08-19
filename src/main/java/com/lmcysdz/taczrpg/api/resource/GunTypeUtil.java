package com.lmcysdz.taczrpg.api.resource;

import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 枪械类型 → 类型专属伤害词条 的映射。
 *
 * <p>枪型取自 TACZ {@code CommonGunIndex.getType()}（如 {@code rifle}/{@code pistol}/...），
 * 映射到对应的 TAA 类型专属伤害属性（如 {@code taa:bullet_gundamage_rifle}）。</p>
 */
public final class GunTypeUtil {

    private GunTypeUtil() {
    }

    /** 本枪的类型专属伤害词条；非枪械 / 无对应类型时返回 null */
    @Nullable
    public static AffixType getTypeDamage(ItemStack stack) {
        if (!(stack.getItem() instanceof IGun iGun)) {
            return null;
        }
        ResourceLocation gunId = iGun.getGunId(stack);
        if (gunId == null) {
            return null;
        }
        String type = TimelessAPI.getCommonGunIndex(gunId)
                .map(index -> index.getType())
                .orElse(null);
        if (type == null) {
            return null;
        }
        return switch (type) {
            case "pistol" -> AffixType.BULLET_PISTOL;
            case "rifle" -> AffixType.BULLET_RIFLE;
            case "shotgun" -> AffixType.BULLET_SHOTGUN;
            case "sniper" -> AffixType.BULLET_SNIPER;
            case "smg" -> AffixType.BULLET_SMG;
            case "mg" -> AffixType.BULLET_LMG;
            case "rpg", "launcher" -> AffixType.BULLET_LAUNCHER;
            default -> null;
        };
    }
}
