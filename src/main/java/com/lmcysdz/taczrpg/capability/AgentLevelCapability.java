package com.lmcysdz.taczrpg.capability;

import com.lmcysdz.taczrpg.TaczRpg;
import com.lmcysdz.taczrpg.network.ModNetwork;
import com.lmcysdz.taczrpg.network.SyncAgentLevelPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public final class AgentLevelCapability {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TaczRpg.MODID, "agent_level");
    public static final Capability<AgentLevel> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private AgentLevelCapability() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(AgentLevel.class);
    }

    /** 便捷获取玩家的特工等级 */
    public static Optional<AgentLevel> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    /** 把服务端特工等级/经验同步给客户端玩家（登录/重生/击杀/setlevel 时调用；仅服务端生效） */
    public static void syncToClient(Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        get(sp).ifPresent(agent -> ModNetwork.sendToPlayer(sp,
                new SyncAgentLevelPacket(agent.getLevel(), agent.getExp())));
    }

    @SubscribeEvent
    public static void attachEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ID, new AgentLevelImpl());
        }
    }
}
