package com.lmcysdz.taczrpg.registry;

import com.lmcysdz.taczrpg.TaczRpg;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 自定义属性：暴击几率 / 暴击伤害。
 *
 * <p>base = 1.0（倍率，1.0 = 无加成），词条以 {@code MULTIPLY_BASE} 修饰符挂载，
 * 与 TAA 属性一致。暴击判定见 {@link com.lmcysdz.taczrpg.event.CritHandler}。</p>
 */
@Mod.EventBusSubscriber(modid = TaczRpg.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TaczRpg.MODID);

    /** 暴击几率：有效值 = 1.0 + 词条加总，实际暴击率 = 有效值 - 1.0 */
    public static final RegistryObject<Attribute> CRIT_CHANCE = ATTRIBUTES.register("crit_chance",
            () -> new RangedAttribute("attribute.name.tacz_rpg.crit_chance", 1.0, 1.0, 1024.0).setSyncable(true));

    /** 暴击伤害：有效值 = 1.0 + 词条加总，暴击时实际伤害 × 有效值 */
    public static final RegistryObject<Attribute> CRIT_DAMAGE = ATTRIBUTES.register("crit_damage",
            () -> new RangedAttribute("attribute.name.tacz_rpg.crit_damage", 1.0, 1.0, 1024.0).setSyncable(true));

    private ModAttributes() {}

    /** 把自定义属性挂到玩家实体，否则 {@code getAttributeValue} 读不到词条修饰符 */
    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, CRIT_CHANCE.get());
        event.add(EntityType.PLAYER, CRIT_DAMAGE.get());
    }
}
