package com.lmcysdz.taczrpg.registry;

import com.lmcysdz.taczrpg.TaczRpg;
import com.lmcysdz.taczrpg.block.entity.CalibrationStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TaczRpg.MODID);

    public static final RegistryObject<BlockEntityType<CalibrationStationBlockEntity>> CALIBRATION_STATION =
            BLOCK_ENTITIES.register("calibration_station",
                    () -> BlockEntityType.Builder.of(CalibrationStationBlockEntity::new,
                            ModBlocks.CALIBRATION_STATION.get()).build(null));

    private ModBlockEntities() {
    }
}
