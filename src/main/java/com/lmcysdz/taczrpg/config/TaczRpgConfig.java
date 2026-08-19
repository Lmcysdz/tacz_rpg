package com.lmcysdz.taczrpg.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Division-style weapon / attachment gear system configuration.
 *
 * <p>Built on a {@link ForgeConfigSpec}; Cloth Config automatically renders an in-game
 * settings screen for this spec, so no manual GUI wiring is required.</p>
 *
 * <p>The actual quality / affix / level tables from the KubeJS prototype
 * ({@code TACZ_ARPG原型V1.1.txt}) will be ported here during refinement.</p>
 */
public class TaczRpgConfig
{
    public static final ForgeConfigSpec SPEC;

    // 全境封锁系统总开关
    public static final ForgeConfigSpec.BooleanValue ENABLE_DIVISION_SYSTEM;

    // 全局词条倍率（占位，后续接词条池）
    public static final ForgeConfigSpec.DoubleValue GLOBAL_ATTR_MULTIPLIER;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("division");

        ENABLE_DIVISION_SYSTEM = builder
                .comment("Enable the Division-style weapon & attachment gear system.")
                .define("enableDivisionSystem", true);

        GLOBAL_ATTR_MULTIPLIER = builder
                .comment("Global multiplier applied to all rolled attribute affixes.")
                .defineInRange("globalAttrMultiplier", 1.0, 0.0, 100.0);

        builder.pop();

        SPEC = builder.build();
    }
}
