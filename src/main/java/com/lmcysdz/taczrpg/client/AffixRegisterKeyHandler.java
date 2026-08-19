package com.lmcysdz.taczrpg.client;

import com.lmcysdz.taczrpg.client.gui.CalibrationStationGui;
import com.lmcysdz.taczrpg.registry.ModBlocks;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.input.InteractKey;
import com.tacz.guns.util.InputExtraCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 复用 TACZ 交互键（O 键）—— 在校准站附近且持枪时打开校准 GUI。
 * 距离限制：玩家必须在校准站方块 3 格范围内。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AffixRegisterKeyHandler {

    private static final double RANGE = 3.0;

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (!InteractKey.INTERACT_KEY.matches(event.getKey(), event.getScanCode())) return;
        tryOpenGui();
    }

    @SubscribeEvent
    public static void onMousePress(InputEvent.MouseButton.Post event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (!InteractKey.INTERACT_KEY.matchesMouse(event.getButton())) return;
        tryOpenGui();
    }

    private static void tryOpenGui() {
        if (!InputExtraCheck.isInGame()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IGun)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.tacz_rpg.need_gun"), true);
            return;
        }

        // 检查是否在校准站附近
        if (!isNearCalibrationStation(player)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.tacz_rpg.need_station"), true);
            return;
        }

        CalibrationStationGui.open();
    }

    private static boolean isNearCalibrationStation(LocalPlayer player) {
        BlockPos center = player.blockPosition();
        for (int x = -1; x <= 1; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    Block block = player.level().getBlockState(pos).getBlock();
                    if (block == ModBlocks.CALIBRATION_STATION.get()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}