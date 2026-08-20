package com.lmcysdz.taczrpg.network;

import com.lmcysdz.taczrpg.TaczRpg;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("tacz_rpg", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(id++, CalibrationActionPacket.class,
                CalibrationActionPacket::encode, CalibrationActionPacket::decode,
                CalibrationActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, CalibrationResultPacket.class,
                CalibrationResultPacket::encode, CalibrationResultPacket::decode,
                CalibrationResultPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SyncAffixLibraryPacket.class,
                SyncAffixLibraryPacket::encode, SyncAffixLibraryPacket::decode,
                SyncAffixLibraryPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SyncAgentLevelPacket.class,
                SyncAgentLevelPacket::encode, SyncAgentLevelPacket::decode,
                SyncAgentLevelPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CalibrationResultPacket packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToPlayer(ServerPlayer player, SyncAffixLibraryPacket packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToPlayer(ServerPlayer player, SyncAgentLevelPacket packet) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}