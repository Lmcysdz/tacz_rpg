package com.lmcysdz.taczrpg.client.render;

import com.lmcysdz.taczrpg.block.CalibrationStationBlock;
import com.lmcysdz.taczrpg.block.entity.CalibrationStationBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Optional;

/**
 * 复用 TACZ gun_smith_table 的 Bedrock 几何模型与材质渲染校准站。
 */
public class CalibrationStationRenderer implements BlockEntityRenderer<CalibrationStationBlockEntity> {

    private static final ResourceLocation GUN_SMITH_TABLE = new ResourceLocation("tacz", "gun_smith_table");

    public CalibrationStationRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CalibrationStationBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Optional<ClientBlockIndex> index = TimelessAPI.getClientBlockIndex(GUN_SMITH_TABLE);
        if (index.isEmpty()) {
            return;
        }
        BedrockModel model = index.get().getModel();
        ResourceLocation texture = index.get().getTexture();
        if (model == null || texture == null) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(CalibrationStationBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(parseRotation(facing)));
        RenderType renderType = RenderType.entityCutout(texture);
        model.render(poseStack, ItemDisplayContext.NONE, renderType, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();
    }

    private static float parseRotation(Direction direction) {
        return 90.0F * (3 - direction.get2DDataValue()) - 90;
    }

    @Override
    public boolean shouldRenderOffScreen(CalibrationStationBlockEntity blockEntity) {
        return true;
    }
}
