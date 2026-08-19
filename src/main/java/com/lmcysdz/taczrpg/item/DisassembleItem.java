package com.lmcysdz.taczrpg.item;

import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 枪械分解工具类 —— 不创建实际物品，直接给玩家发放材料 Item（通过材料 tag）。
 */
public final class DisassembleItem {

    private DisassembleItem() {}

    /**
     * 根据专精等级计算分解出的零件数量（1-12个）。
     * 等级越高，保底和上限越高。
     *
     * @param expertiseLevel 枪械的专精等级
     * @return 分解出的零件数量
     */
    public static int calculatePartCount(int expertiseLevel) {
        if (expertiseLevel <= 0) return 1;
        if (expertiseLevel >= 20) return 12;
        return 1 + expertiseLevel / 2;
    }

    /**
     * 执行分解：移除主手枪械，发放材料物品给玩家背包。
     *
     * @param player 玩家
     * @return 分解出的材料数量（背包可能放不下，部分掉落）
     */
    public static int disassemble(Player player) {
        ItemStack gun = player.getMainHandItem();
        if (gun.isEmpty() || !(gun.getItem() instanceof IGun)) {
            return 0;
        }

        // 读取专精等级
        int expertiseLevel = gun.getOrCreateTag().getInt(AffixSystem.EXPERTISE_LEVEL_TAG);
        if (expertiseLevel == 0) {
            expertiseLevel = 1; // 至少给1个
        }

        int count = calculatePartCount(expertiseLevel);

        // 移除枪支（扣1个）
        gun.shrink(1);

        // 发放通用材料（使用校准材料 tag 中的物品）
        // 这里发放与 AgentLevel 匹配的材料品质
        // 简单实现：发给玩家一个材料物品，使用 ModItems 里已注册的
        var material = com.lmcysdz.taczrpg.registry.ModItems.MATERIALS.get(
                com.lmcysdz.taczrpg.api.resource.MaterialQuality.UNCOMMON
        );
        if (material == null) return count;

        ItemStack matStack = new ItemStack(material.get(), count);
        if (!player.getInventory().add(matStack)) {
            player.drop(matStack, false);
        }
        return count;
    }
}