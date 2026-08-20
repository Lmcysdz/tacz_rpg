package com.lmcysdz.taczrpg.client;

import com.lmcysdz.taczrpg.client.gui.CalibrationStationGui;
import com.lmcysdz.taczrpg.registry.ModBlocks;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.util.InputExtraCheck;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 校准站快捷键（默认 O）—— 在校准站附近且持枪时打开校准 GUI。
 * 距离限制：玩家必须在校准站方块 3 格范围内。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AffixRegisterKeyHandler {

    private static final double RANGE = 3.0;

    /** 校准站快捷键（独立注册，可在按键设置中修改；由 TaczRpg.ClientModEvents 注册） */
    public static final KeyMapping CALIBRATION_KEY = new KeyMapping(
            "key.tacz_rpg.open_calibration",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.tacz_rpg");

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (!CALIBRATION_KEY.matches(event.getKey(), event.getScanCode())) return;
        tryOpenGui();
    }

    @SubscribeEvent
    public static void onMousePress(InputEvent.MouseButton.Post event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (!CALIBRATION_KEY.matchesMouse(event.getButton())) return;
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