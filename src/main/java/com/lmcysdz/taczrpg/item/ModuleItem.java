package com.lmcysdz.taczrpg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 模组数据块（ModuleItem）—— 优化操作消耗的特殊材料。
 * 无法通过正常拆枪获得，需通过特殊途径获取。
 */
public class ModuleItem extends Item {

    public ModuleItem() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.tacz_rpg.module_item.desc").withStyle(ChatFormatting.GOLD));
    }
}