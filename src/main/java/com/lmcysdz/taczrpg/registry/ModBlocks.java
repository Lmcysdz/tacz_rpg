package com.lmcysdz.taczrpg.registry;

import com.lmcysdz.taczrpg.TaczRpg;
import com.lmcysdz.taczrpg.block.CalibrationStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TaczRpg.MODID);

    /** 校准站 —— 复用 TACZ gun_smith_table 材质 */
    public static final RegistryObject<Block> CALIBRATION_STATION =
            BLOCKS.register("calibration_station", CalibrationStationBlock::new);

    private ModBlocks() {
    }
}
