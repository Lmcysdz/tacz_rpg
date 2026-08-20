package com.lmcysdz.taczrpg.network;

import com.lmcysdz.taczrpg.capability.AgentLevelCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 客户端同步特工等级与经验数据包。
 *
 * <p>登录 / 重生 / 击杀 / setlevel 命令时发送，供校准站与 Tooltip 显示等级进度。</p>
 */
public class SyncAgentLevelPacket {

    private final int level;
    private final int exp;

    public SyncAgentLevelPacket(int level, int exp) {
        this.level = level;
        this.exp = exp;
    }

    public static void encode(SyncAgentLevelPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.exp);
    }

    public static SyncAgentLevelPacket decode(FriendlyByteBuf buf) {
        return new SyncAgentLevelPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SyncAgentLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> apply(msg.level, msg.exp));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void apply(int level, int exp) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        AgentLevelCapability.get(player).ifPresent(agent -> agent.setFromSync(level, exp));
    }
}
