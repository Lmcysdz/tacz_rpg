package com.lmcysdz.taczrpg.block;

import com.lmcysdz.taczrpg.block.entity.CalibrationStationBlockEntity;
import com.lmcysdz.taczrpg.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

/**
 * 校准站方块 —— 复用 TACZ gun_smith_table 的 Bedrock 几何模型渲染（见 {@link RenderShape#ENTITYBLOCK_ANIMATED}）。
 *
 * <p>右键由客户端打开 Cloth Config 风格的校准站 GUI（三标签页：校准/优化/专精），
 * 物品直接操作手持物品，不走容器槽位。</p>
 */
public class CalibrationStationBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CalibrationStationBlock() {
        super(Properties.of().sound(SoundType.WOOD).strength(2.0F, 3.0F).noOcclusion());
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            return InteractionResult.sidedSuccess(false);
        }
        // 客户端打开 Cloth Config 校准站 GUI（纯手持交互，无需容器/服务端同步）
        openGui();
        return InteractionResult.sidedSuccess(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void openGui() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.lmcysdz.taczrpg.client.gui.CalibrationStationGui.open());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.CALIBRATION_STATION.get().create(pos, state);
    }
}
