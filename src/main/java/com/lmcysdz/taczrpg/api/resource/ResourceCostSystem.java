package com.lmcysdz.taczrpg.api.resource;

import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ResourceCostSystem {

    // ===== 普通武器 优化 消耗（档位材料 + 模组数据块） =====
    /** 普通武器每次优化消耗的档位材料数量 */
    public static final int OPTIMIZE_TIER_MATERIAL = 1;
    /** 普通武器每次优化消耗的模组数据块数量 */
    public static final int OPTIMIZE_MODULE = 1;

    // ===== 奇特武器 消耗 / 奖励 常量（可调） =====
    /** 奇特武器每次优化消耗的终极材料数量 */
    public static final int EXOTIC_OPTIMIZE_ULTIMATE = 1;
    /** 奇特武器每次优化消耗的模组数据块数量 */
    public static final int EXOTIC_OPTIMIZE_MODULE = 1;
    /** 奇特武器分解获得的终极材料数量 */
    public static final int EXOTIC_DISASSEMBLE_ULTIMATE = 3;
    /** 奇特武器分解获得的模组数据块数量 */
    public static final int EXOTIC_DISASSEMBLE_MODULE = 1;

    private ResourceCostSystem() {}

    /**
     * 品质档（词条等级档）→ 材料品质：
     * 0 基础(常见) / 1 稀有 / 2 精英 / 3 传说；终极材料仅由奇特武器产生。
     */
    public static MaterialQuality rankToMaterial(int rankIndex) {
        return switch (rankIndex) {
            case 0 -> MaterialQuality.UNCOMMON;
            case 1 -> MaterialQuality.RARE;
            case 2 -> MaterialQuality.EPIC;
            default -> MaterialQuality.LEGENDARY;
        };
    }

    /** 枪械的品质档材料物品（按枪械等级快照的档位） */
    public static Item getTierMaterial(ItemStack gun) {
        int ri = AffixSystem.getRankIndex(gun.getOrCreateTag().getInt(AffixSystem.LEVEL_TAG));
        return ModItems.MATERIALS.get(rankToMaterial(ri)).get();
    }
    public static int getCalibrationBaseCost(int ri) {
        return switch (ri) { case 0 -> 2; case 1 -> 3; case 2 -> 4; case 3 -> 5; default -> 6; };
    }
    public static int getCalibrationPerCost(int ri) {
        return switch (ri) { case 0, 1 -> 1; case 2, 3 -> 2; default -> 3; };
    }
    public static int getCalibrationTotalCost(int ri, int cc) {
        return getCalibrationBaseCost(ri) + cc * getCalibrationPerCost(ri);
    }
    /** 分解固定数量 */
    public static final int DISASSEMBLE_COUNT = 3;

    public static int getDisassembleCount(int el) {
        return DISASSEMBLE_COUNT;
    }

    /** 按具体物品计数（跨槽位叠加） */
    public static int countItem(Player p, Item item) {
        int c = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() == item) c += s.getCount();
        }
        return c;
    }

    /** 按具体物品消耗 */
    public static boolean tryConsumeItem(Player p, Item item, int n) {
        if (countItem(p, item) < n) return false;
        int r = n;
        for (int i = 0; i < p.getInventory().getContainerSize() && r > 0; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.isEmpty() || s.getItem() != item) continue;
            int rm = Math.min(r, s.getCount());
            s.shrink(rm); r -= rm;
        }
        return true;
    }
}