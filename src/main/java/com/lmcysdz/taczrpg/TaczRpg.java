package com.lmcysdz.taczrpg;

import com.lmcysdz.taczrpg.api.exotic.ExoticWeaponManager;
import com.lmcysdz.taczrpg.capability.AffixLibraryCapability;
import com.lmcysdz.taczrpg.capability.AgentLevelCapability;
import com.lmcysdz.taczrpg.client.AffixRegisterKeyHandler;
import com.lmcysdz.taczrpg.client.AffixTooltip;
import com.lmcysdz.taczrpg.client.gui.CalibrationStationGui;
import com.lmcysdz.taczrpg.client.render.CalibrationStationRenderer;
import com.lmcysdz.taczrpg.command.TaczRpgCommand;
import com.lmcysdz.taczrpg.config.TaczRpgConfig;
import com.lmcysdz.taczrpg.event.AffixAttributeHandler;
import com.lmcysdz.taczrpg.event.AutoRegisterHandler;
import com.lmcysdz.taczrpg.event.CritHandler;
import com.lmcysdz.taczrpg.event.ExpertiseHandler;
import com.lmcysdz.taczrpg.network.ModNetwork;
import com.lmcysdz.taczrpg.registry.ModAttributes;
import com.lmcysdz.taczrpg.registry.ModBlockEntities;
import com.lmcysdz.taczrpg.registry.ModBlocks;
import com.lmcysdz.taczrpg.registry.ModCreativeTabs;
import com.lmcysdz.taczrpg.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TaczRpg.MODID)
public class TaczRpg {
    public static final String MODID = "tacz_rpg";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TaczRpg(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        ModNetwork.register();

        context.registerConfig(ModConfig.Type.COMMON, TaczRpgConfig.SPEC);

        modEventBus.addListener(AgentLevelCapability::register);
        modEventBus.addListener(AffixLibraryCapability::register);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(AgentLevelCapability.class);
        MinecraftForge.EVENT_BUS.register(AffixLibraryCapability.class);
        MinecraftForge.EVENT_BUS.register(AffixAttributeHandler.class);
        MinecraftForge.EVENT_BUS.register(ExpertiseHandler.class);
        MinecraftForge.EVENT_BUS.register(AutoRegisterHandler.class);
        MinecraftForge.EVENT_BUS.register(CritHandler.class);
        MinecraftForge.EVENT_BUS.register(ExoticWeaponManager.class);

        // 注册命令
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        LOGGER.info("TACZ RPG: Division-style weapon gear system initialized");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        TaczRpgCommand.register(event.getDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(AffixRegisterKeyHandler.CALIBRATION_KEY);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.CALIBRATION_STATION.get(), CalibrationStationRenderer::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MinecraftForge.EVENT_BUS.register(AffixTooltip.class));
        }
    }
}