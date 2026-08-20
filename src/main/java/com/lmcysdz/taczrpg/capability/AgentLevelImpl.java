package com.lmcysdz.taczrpg.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AgentLevelImpl implements AgentLevel, ICapabilitySerializable<CompoundTag> {

    private static final int MAX_LEVEL = 60;
    private static final int BASE_EXP = 100;
    private static final int EXP_PER_LEVEL = 50;

    private int level;
    private int exp;

    private final LazyOptional<AgentLevel> holder = LazyOptional.of(() -> this);

    @Override
    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = Math.min(level, MAX_LEVEL); }

    public void setExp(int exp) { this.exp = exp; }

    @Override
    public void setFromSync(int level, int exp) {
        this.level = Math.min(level, MAX_LEVEL);
        this.exp = Math.max(0, exp);
    }

    @Override
    public int getExp() { return exp; }

    @Override
    public int getExpToNextLevel() {
        if (level >= MAX_LEVEL) return 0;
        return BASE_EXP + level * EXP_PER_LEVEL;
    }

    @Override
    public void addExp(int amount) {
        if (amount <= 0) return;
        this.exp += amount;
        while (level < MAX_LEVEL && exp >= getExpToNextLevel()) {
            exp -= getExpToNextLevel();
            level++;
        }
        if (level >= MAX_LEVEL) exp = 0;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == AgentLevelCapability.CAPABILITY) return holder.cast();
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putInt("exp", exp);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.level = tag.getInt("level");
        this.exp = tag.getInt("exp");
    }
}