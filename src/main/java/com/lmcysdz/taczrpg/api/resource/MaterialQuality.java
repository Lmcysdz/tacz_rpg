package com.lmcysdz.taczrpg.api.resource;

/**
 * 材料品质：校准/优化消耗的资源分级。
 */
public enum MaterialQuality {
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary"),
    ULTIMATE("ultimate");

    private final String suffix;

    MaterialQuality(String suffix) {
        this.suffix = suffix;
    }

    /** 注册名后缀：uncommon / rare / ... */
    public String suffix() {
        return suffix;
    }

    /** 物品注册名：tacz_rpg:<suffix>_material */
    public String itemName() {
        return suffix + "_material";
    }
}
