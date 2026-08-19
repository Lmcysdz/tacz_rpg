package com.lmcysdz.taczrpg.capability;

import java.util.Map;

/**
 * 词条库 —— 玩家提取过的词条及其最高数值（类似全境封锁2的校准库）。
 */
public interface AffixLibrary {
    Map<String, Float> getAll();
    Float get(String affixKey);
    void put(String affixKey, float value);
    boolean has(String affixKey);
}