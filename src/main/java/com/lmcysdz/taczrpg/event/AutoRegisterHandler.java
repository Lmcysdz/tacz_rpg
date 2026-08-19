package com.lmcysdz.taczrpg.event;

import com.lmcysdz.taczrpg.api.affix.AffixAttribution;
import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.capability.AgentLevel;
import com.lmcysdz.taczrpg.capability.AgentLevelCapability;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.IAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 自动词条注册 —— 玩家拿到枪/配件的瞬间就完成注册，不再需要按 O 键手动注册。
 *
 * <p>覆盖两个时机：</p>
 * <ul>
 *   <li>{@link LivingEquipmentChangeEvent} — 玩家切换主手/副手/盔甲槽时触发</li>
 *   <li>{@link PlayerEvent.PlayerLoggedInEvent} — 玩家进世界时检查背包中已有枪支</li>
 * </ul>
 */
public class AutoRegisterHandler {

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // 只关心主手和副手槽
        var slot = event.getSlot();
        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND
                && slot != net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
            return;
        }
        // 检查新切换到的物品
        ItemStack to = event.getTo();
        tryRegister(player, to);
        // 如果从主手切到副手等情况，也检查另一只手
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand != to) {
            tryRegister(player, mainHand);
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand != to) {
            tryRegister(player, offHand);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // 玩家进世界时扫描背包所有物品
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            tryRegister(player, player.getInventory().getItem(i));
        }
    }

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        tryRegister(player, event.getStack());
        // 捡起后也可能直接进了主手/副手
        tryRegister(player, player.getMainHandItem());
        tryRegister(player, player.getOffhandItem());
    }

    /**
     * 兜底登记：每 20 tick（约 1 秒）扫描一次背包。
     * 覆盖创造栏 / 合成 / /give 等不触发 ItemPickupEvent 的获取途径，
     * 确保任何来源的枪械/配件都能被登记上词条。
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            tryRegister(player, player.getInventory().getItem(i));
        }
    }

    /**
     * 对单件物品尝试注册词条（幂等：已注册的跳过）。
     */
    private static void tryRegister(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        boolean isGun = stack.getItem() instanceof IGun;
        boolean isAttachment = stack.getItem() instanceof IAttachment;
        if (!isGun && !isAttachment) {
            return;
        }
        // 已注册 → 跳过
        if (AffixAttribution.isRegistered(stack)) {
            return;
        }
        int agentLevel = AgentLevelCapability.get(player)
                .map(AgentLevel::getLevel)
                .orElse(0);
        AffixAttribution.applyAffixes(stack, agentLevel, player.getRandom());
    }
}