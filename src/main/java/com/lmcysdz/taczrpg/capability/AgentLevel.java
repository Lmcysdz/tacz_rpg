package com.lmcysdz.taczrpg.capability;

/**
 * 特工等级：玩家级等级，区别于 MC 经验等级。决定词条注册的品质档。
 */
public interface AgentLevel {

    int getLevel();

    int getExp();

    /** 增加经验，满级自动升级 */
    void addExp(int amount);

    /** 当前等级升到下一级所需经验 */
    int getExpToNextLevel();

    /** 客户端同步：直接设置等级与经验（由同步包调用） */
    void setFromSync(int level, int exp);
}
