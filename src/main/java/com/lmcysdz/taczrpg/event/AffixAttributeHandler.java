package com.lmcysdz.taczrpg.event;

import com.lmcysdz.taczrpg.api.affix.AffixSystem;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.lmcysdz.taczrpg.config.TaczRpgConfig;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 把枪械本体与已挂载配件的词条，映射为 TAA 属性修饰器。
 *
 * <p>对应 KubeJS 原型 {@code Attributes_Test.js}。额外挂载专精伤害加成。</p>
 */
public class AffixAttributeHandler {

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack gun = event.getItemStack();
        if (!(gun.getItem() instanceof IGun iGun)) {
            return;
        }
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }

        // 基础弹容/弹丸（固定值词条按枪基础换算倍率，枪本体与配件槽共用）
        int baseAmmo = 30;
        int basePellets = 1;
        ResourceLocation gunId = iGun.getGunId(gun);
        if (gunId != null) {
            var index = TimelessAPI.getCommonGunIndex(gunId);
            if (index.isPresent()) {
                var gunData = index.get().getGunData();
                baseAmmo = gunData.getAmmoAmount();
                if (gunData.getBulletData() != null) {
                    basePellets = gunData.getBulletData().getBulletAmount();
                }
            }
        }

        // 枪械本体词条
        applyTagAffixes(event, gun.getOrCreateTag(), "gun", baseAmmo, basePellets);

        // 各配件槽位词条（config 关闭配件词条时跳过，避免「枪 + 配件」词条过多）
        if (TaczRpgConfig.ENABLE_ATTACHMENT_AFFIXES.get()) {
            for (AttachmentType type : AttachmentType.values()) {
                if (type == AttachmentType.NONE) {
                    continue;
                }
                // getAttachmentTag 内部已解包，直接返回配件词条 NBT（含 AttachmentId + 词条）
                CompoundTag attachmentTag = iGun.getAttachmentTag(gun, type);
                if (attachmentTag != null) {
                    applyTagAffixes(event, attachmentTag, "attachment_" + type.name(), baseAmmo, basePellets);
                }
            }
        }
        // 专精为固定伤害（每级 +1），在 EntityHurtByGunEvent.Pre 中应用（见 ExpertiseHandler）
    }

    private static void applyTagAffixes(ItemAttributeModifierEvent event, CompoundTag tag, String slotName,
                                        int baseAmmo, int basePellets) {
        for (AffixType affix : AffixSystem.AFFIX_POOL) {
            if (!tag.contains(affix.key(), Tag.TAG_DOUBLE)) {
                continue;
            }
            double value = tag.getDouble(affix.key());
            if (value == 0d) {
                continue;
            }
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(affix.attributeId());
            if (attribute == null) {
                continue;
            }
            // 固定值词条（弹容/弹丸）：按枪基础换算成倍率，TAA 截断后精确 +N（(base+N+0.5)/base − 1）
            double modifierValue = value;
            if (AffixSystem.isFlat(affix)) {
                int base = affix == AffixType.MAGAZINE_CAPACITY ? baseAmmo : basePellets;
                modifierValue = (base + value + 0.5) / base - 1.0;
            }
            UUID uuid = UUID.nameUUIDFromBytes((affix.key() + "_" + slotName).getBytes(StandardCharsets.UTF_8));
            event.addModifier(attribute, new AttributeModifier(
                    uuid, "tacz_rpg_" + affix.key(), modifierValue, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
}
