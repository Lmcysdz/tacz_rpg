package com.lmcysdz.taczrpg.block.entity;

import com.lmcysdz.taczrpg.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 校准站方块实体：仅承载渲染（复用 TACZ gun_smith_table 模型），无容器逻辑。
 * 物品交互直接作用于手持物品，不走槽位。
 */
public class CalibrationStationBlockEntity extends BlockEntity {

    public CalibrationStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CALIBRATION_STATION.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(2, 1, 2));
    }
}
