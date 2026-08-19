package com.lmcysdz.taczrpg.registry;

import com.lmcysdz.taczrpg.TaczRpg;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeapon;
import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组专属创造标签页：TACZ RPG 相关物品（校准站 / 提取物 / 模组数据块 / 品质材料）+ 奇特武器。
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TaczRpg.MODID);

    public static final RegistryObject<CreativeModeTab> TACZ_RPG = TABS.register("tacz_rpg", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tacz_rpg"))
                    .icon(() -> new ItemStack(ModItems.CALIBRATION_STATION.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.CALIBRATION_STATION.get());
                        output.accept(ModItems.AFFIX_EXTRACT.get());
                        output.accept(ModItems.MODULE_ITEM.get());
                        ModItems.MATERIALS.values().forEach(reg -> output.accept(reg.get()));
                        // 奇特武器（数据包定义）
                        for (ExoticWeapon exotic : ExoticWeaponManager.getAll()) {
                            ItemStack stack = ExoticWeaponManager.createExoticStack(exotic);
                            if (!stack.isEmpty()) {
                                output.accept(stack);
                            }
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
