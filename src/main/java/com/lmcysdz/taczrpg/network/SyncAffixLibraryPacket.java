package com.lmcysdz.taczrpg.network;

import com.lmcysdz.taczrpg.capability.AffixLibrary;
import com.lmcysdz.taczrpg.capability.AffixLibraryCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 服务端 → 客户端同步词条库数据包。
 *
 * <p>玩家打开校准 GUI、或词条库发生变化时发送。</p>
 */
public class SyncAffixLibraryPacket {

    private final Map<String, Float> data;

    public SyncAffixLibraryPacket(Map<String, Float> data) {
        this.data = data;
    }

    public static void encode(SyncAffixLibraryPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.data.size());
        for (Map.Entry<String, Float> entry : msg.data.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeFloat(entry.getValue());
        }
    }

    public static SyncAffixLibraryPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, Float> data = new HashMap<>();
        for (int i = 0; i < size; i++) {
            data.put(buf.readUtf(), buf.readFloat());
        }
        return new SyncAffixLibraryPacket(data);
    }

    public static void handle(SyncAffixLibraryPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> apply(msg.data));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void apply(Map<String, Float> data) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        AffixLibraryCapability.get(player).ifPresent(lib -> {
            for (Map.Entry<String, Float> entry : data.entrySet()) {
                lib.put(entry.getKey(), entry.getValue());
            }
        });
    }
}