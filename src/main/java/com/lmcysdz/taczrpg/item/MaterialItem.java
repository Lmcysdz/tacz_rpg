package com.lmcysdz.taczrpg.item;

import com.lmcysdz.taczrpg.api.resource.MaterialQuality;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 品质材料物品：校准/优化功能的消耗资源。
 *
 * <p>有 5 种品质（uncommon/rare/epic/legendary/ultimate），对应贴图
 * {@code textures/item/<name>_material.png}。</p>
 */
public class MaterialItem extends Item {

    private final MaterialQuality quality;

    public MaterialItem(MaterialQuality quality) {
        super(new Item.Properties());
        this.quality = quality;
    }

    public MaterialQuality getQuality() {
        return quality;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tacz_rpg.material_quality")
                .append(Component.translatable("material.tacz_rpg." + quality.suffix()).withStyle(qualityColor())));
    }

    private ChatFormatting qualityColor() {
        return switch (quality) {
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.BLUE;
            case EPIC -> ChatFormatting.DARK_PURPLE;
            case LEGENDARY -> ChatFormatting.GOLD;
            case ULTIMATE -> ChatFormatting.AQUA;
        };
    }
}
