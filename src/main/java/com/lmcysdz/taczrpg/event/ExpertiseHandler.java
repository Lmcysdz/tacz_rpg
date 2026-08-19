package com.lmcysdz.taczrpg.event;

import com.lmcysdz.taczrpg.api.affix.AffixOperation;
import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.capability.AgentLevel;
import com.lmcysdz.taczrpg.capability.AgentLevelCapability;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.item.IGun;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * 专精经验获取：枪械击杀 → 枪械专精经验 + 特工等级经验。
 */
public class ExpertiseHandler {

    @SubscribeEvent
    public static void onKillByGun(EntityKillByGunEvent event) {
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        LivingEntity attacker = event.getAttacker();
        if (!(attacker instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof IGun)) {
            return;
        }

        // 枪械专精经验（任何生物击杀都算，不限敌对，含奇特武器）
        boolean leveledUp = AffixOperation.addExpertiseExp(held, AffixOperation.EXP_PER_KILL);

        // 特工等级经验（每击杀 +1）
        AgentLevelCapability.get(player).ifPresent(agent -> agent.addExp(1));

        // 击杀反馈（未满级才提示）
        int lv = AffixOperation.getExpertiseLevel(held);
        if (lv < AffixOperation.MAX_EXPERTISE_LEVEL) {
            int exp = held.getOrCreateTag().getInt(AffixSystem.EXPERTISE_EXP_TAG);
            player.displayClientMessage(Component.literal("§7专精经验 +1（Lv " + lv + " · " + exp + "/" + AffixOperation.EXP_TO_LEVEL + "）"), true);
        }
        if (leveledUp) {
            player.displayClientMessage(Component.translatable("message.tacz_rpg.expertise_level_up"), true);
        }
    }

    /**
     * 专精固定伤害：每级 +1 平伤（上限 +30），枪击命中时叠加到基础伤害。
     * 任意枪械（含奇特）都有专精，专精为固定数值而非百分比。
     */
    @SubscribeEvent
    public static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        LivingEntity attacker = event.getAttacker();
        if (!(attacker instanceof Player player)) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof IGun)) {
            return;
        }
        int lv = AffixOperation.getExpertiseLevel(held);
        if (lv > 0) {
            event.setBaseAmount(event.getBaseAmount() + lv);
        }
    }
}
