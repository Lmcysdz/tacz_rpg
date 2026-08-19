package com.lmcysdz.taczrpg.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 客户端的操作结果包。
 */
public class CalibrationResultPacket {

    private final CalibrationActionPacket.Action action;
    private final int result;

    public CalibrationResultPacket(CalibrationActionPacket.Action action, int result) {
        this.action = action;
        this.result = result;
    }

    public static void encode(CalibrationResultPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeInt(msg.result);
    }

    public static CalibrationResultPacket decode(FriendlyByteBuf buf) {
        return new CalibrationResultPacket(buf.readEnum(CalibrationActionPacket.Action.class), buf.readInt());
    }

    public static void handle(CalibrationResultPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> showResult(msg)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void showResult(CalibrationResultPacket msg) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (msg.result == 0) {
            // 分解成功的消息已由服务端带数量显示（actionbar），这里跳过避免无参数 %s 覆盖
            if (msg.action == CalibrationActionPacket.Action.DISASSEMBLE) {
                return;
            }
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("message.tacz_rpg.action.success." + msg.action.name().toLowerCase()), true);
        } else {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("message.tacz_rpg.action.fail." + msg.action.name().toLowerCase() + "." + msg.result), true);
        }
    }
}
