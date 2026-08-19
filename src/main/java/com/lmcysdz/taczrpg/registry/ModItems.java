package com.lmcysdz.taczrpg.registry;

import com.lmcysdz.taczrpg.TaczRpg;
import com.lmcysdz.taczrpg.api.resource.MaterialQuality;
import com.lmcysdz.taczrpg.item.AffixExtractItem;
import com.lmcysdz.taczrpg.item.MaterialItem;
import com.lmcysdz.taczrpg.item.ModuleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TaczRpg.MODID);

    public static final RegistryObject<Item> CALIBRATION_STATION =
            ITEMS.register("calibration_station",
                    () -> new BlockItem(ModBlocks.CALIBRATION_STATION.get(), new Item.Properties()));

    public static final RegistryObject<Item> AFFIX_EXTRACT =
            ITEMS.register("affix_extract", AffixExtractItem::new);

    /** 模组数据块 —— 优化操作消耗品，无法通过拆枪获得 */
    public static final RegistryObject<Item> MODULE_ITEM =
            ITEMS.register("module_item", ModuleItem::new);

    /** 品质材料（5种）—— 校准/优化/分解消耗 */
    public static final Map<MaterialQuality, RegistryObject<Item>> MATERIALS = new EnumMap<>(MaterialQuality.class);

    static {
        for (MaterialQuality quality : MaterialQuality.values()) {
            MATERIALS.put(quality, ITEMS.register(quality.itemName(), () -> new MaterialItem(quality)));
        }
    }

    private ModItems() {}
}