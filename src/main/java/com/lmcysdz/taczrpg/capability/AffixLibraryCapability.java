package com.lmcysdz.taczrpg.capability;

import com.lmcysdz.taczrpg.TaczRpg;
import com.lmcysdz.taczrpg.network.ModNetwork;
import com.lmcysdz.taczrpg.network.SyncAffixLibraryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Optional;

public final class AffixLibraryCapability {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TaczRpg.MODID, "affix_library");
    public static final Capability<AffixLibrary> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private AffixLibraryCapability() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(AffixLibrary.class);
    }

    public static Optional<AffixLibrary> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    @SubscribeEvent
    public static void onAttachEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ID, new AffixLibraryImpl());
        }
    }

    /** 进世界时把词条库同步到客户端，避免校准页显示"词条库为空" */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncToClient(player);
        }
    }

    /** 死亡后克隆词条库（永久存档），并把数据同步到新客户端 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getOriginal() instanceof ServerPlayer original)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;
        get(original).ifPresent(origLib ->
                get(newPlayer).ifPresent(newLib -> {
                    for (Map.Entry<String, Float> e : origLib.getAll().entrySet()) {
                        newLib.put(e.getKey(), e.getValue());
                    }
                }));
    }

    /** 重生后重新同步词条库到客户端 */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncToClient(player);
        }
    }

    private static void syncToClient(ServerPlayer player) {
        get(player).ifPresent(lib -> ModNetwork.sendToPlayer(player, new SyncAffixLibraryPacket(lib.getAll())));
    }
}