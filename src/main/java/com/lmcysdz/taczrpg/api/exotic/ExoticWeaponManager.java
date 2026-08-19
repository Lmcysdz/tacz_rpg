package com.lmcysdz.taczrpg.api.exotic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lmcysdz.taczrpg.api.affix.AffixAttribution;
import com.lmcysdz.taczrpg.api.affix.AffixType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 奇特武器识别接口（数据驱动）。
 *
 * <p>数据包在 {@code data/tacz_rpg/exotic_weapons/} 目录放 JSON 定义（见 {@link ExoticWeapon}），
 * 重载数据包后自动加载进注册表。按枪械 ID 识别：</p>
 * <pre>{@code
 * {
 *   "gun": "tacz:ak47",
 *   "display_name": "撕裂者",
 *   "exotic_affix": { "affix": "bullet_rifle", "value": 0.35 },
 *   "affixes": [
 *     { "affix": "crit_damage", "value": 0.10 },
 *     { "affix": "magazine_capacity", "value": 0.08 }
 *   ]
 * }
 * }</pre>
 */
public final class ExoticWeaponManager {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "exotic_weapons";

    private static final SimpleJsonResourceReloadListener LISTENER = new SimpleJsonResourceReloadListener(GSON, DIRECTORY) {
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, ExoticWeapon> loaded = new HashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : elements.entrySet()) {
                try {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    ResourceLocation gunId = new ResourceLocation(obj.get("gun").getAsString());
                    String displayName = obj.get("display_name").getAsString();
                    JsonObject exo = obj.getAsJsonObject("exotic_affix");
                    AffixType exoticAffix = AffixType.byKey(exo.get("affix").getAsString());
                    double exoticValue = exo.get("value").getAsDouble();
                    if (exoticAffix == null) {
                        continue;
                    }
                    List<ExoticWeapon.ExoticAffix> affixes = new ArrayList<>();
                    if (obj.has("affixes")) {
                        for (JsonElement e : obj.getAsJsonArray("affixes")) {
                            JsonObject a = e.getAsJsonObject();
                            AffixType t = AffixType.byKey(a.get("affix").getAsString());
                            if (t != null) {
                                affixes.add(new ExoticWeapon.ExoticAffix(t, a.get("value").getAsDouble()));
                            }
                        }
                    }
                    loaded.put(gunId, new ExoticWeapon(gunId, displayName, exoticAffix, exoticValue, affixes));
                } catch (Exception ignored) {
                    // 单个文件解析失败不阻断其他文件
                }
            }
            REGISTRY = loaded;
        }
    };

    /** 当前已加载的奇特武器注册表（按枪械 ID） */
    private static volatile Map<ResourceLocation, ExoticWeapon> REGISTRY = new HashMap<>();

    /** 奇特武器红橙色（RGB） */
    public static final int EXOTIC_RGB = 0xFF6B4A;
    /** TACZ 现代动能枪物品 ID（构建奇特武器用） */
    private static final ResourceLocation GUN_ITEM = new ResourceLocation("tacz", "modern_kinetic_gun");

    private ExoticWeaponManager() {
    }

    /** 该手持物品是否为奇特武器 */
    public static boolean isExotic(ItemStack stack) {
        return getExotic(stack) != null;
    }

    /** 获取该手持物品的奇特定义；非奇特返回 null */
    @Nullable
    public static ExoticWeapon getExotic(ItemStack stack) {
        if (!(stack.getItem() instanceof IGun iGun)) {
            return null;
        }
        ResourceLocation gunId = iGun.getGunId(stack);
        if (gunId == null) {
            return null;
        }
        return REGISTRY.get(gunId);
    }

    /** 按枪械 ID 查询 */
    @Nullable
    public static ExoticWeapon getExotic(ResourceLocation gunId) {
        return gunId == null ? null : REGISTRY.get(gunId);
    }

    /** 当前已加载的全部奇特武器（用于创造标签页等） */
    public static Collection<ExoticWeapon> getAll() {
        return REGISTRY.values();
    }

    /**
     * 构建奇特武器的物品实例（TACZ 现代动能枪 + GunId + 固定词条 + 红橙名字）。
     * 用于创造标签页展示；世界拾取的奇特枪由 AutoRegisterHandler 走同一词条逻辑。
     */
    public static ItemStack createExoticStack(ExoticWeapon exotic) {
        Item gunItem = ForgeRegistries.ITEMS.getValue(GUN_ITEM);
        if (!(gunItem instanceof GunItemDataAccessor accessor)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(gunItem);
        accessor.setGunId(stack, exotic.gunId());
        stack.setHoverName(Component.literal("奇特 " + exotic.displayName())
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(EXOTIC_RGB))));
        AffixAttribution.applyExoticAffixes(stack, exotic, 0);
        return stack;
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(LISTENER);
    }
}
