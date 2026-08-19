package com.lmcysdz.taczrpg.event;

import com.lmcysdz.taczrpg.registry.ModAttributes;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * 暴击判定：枪击命中时按「暴击几率」随机触发，命中后将基础伤害 ×「暴击伤害」倍率。
 *
 * <p>挂在 {@link EntityHurtByGunEvent.Pre}（TACZ 伤害判定前、可改基础伤害的事件）。
 * 两个属性都来自自定义属性（base 1.0），实际暴击率 = {@code crit_chance - 1.0}，
 * 暴击伤害倍率 = {@code crit_damage} 的有效值。</p>
 */
public class CritHandler {

    @SubscribeEvent
    public static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        // 只在服务端判定（客户端预测命中不改实际伤害）
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        LivingEntity attacker = event.getAttacker();
        if (!(attacker instanceof Player player)) {
            return;
        }
        double chance = player.getAttributeValue(ModAttributes.CRIT_CHANCE.get()) - 1.0;
        if (chance <= 0 || player.getRandom().nextDouble() >= chance) {
            return;
        }
        double mult = player.getAttributeValue(ModAttributes.CRIT_DAMAGE.get());
        event.setBaseAmount(event.getBaseAmount() * (float) mult);
    }
}
