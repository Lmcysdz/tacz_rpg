package com.lmcysdz.taczrpg.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AffixLibraryImpl implements AffixLibrary, ICapabilitySerializable<CompoundTag> {

    private final Map<String, Float> library = new HashMap<>();
    private final LazyOptional<AffixLibrary> holder = LazyOptional.of(() -> this);

    @Override
    public Map<String, Float> getAll() {
        return library;
    }

    @Override
    public Float get(String affixKey) {
        return library.get(affixKey);
    }

    @Override
    public void put(String affixKey, float value) {
        Float existing = library.get(affixKey);
        if (existing == null || value > existing) {
            library.put(affixKey, value);
        }
    }

    @Override
    public boolean has(String affixKey) {
        return library.containsKey(affixKey);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<String, Float> entry : library.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey());
            entryTag.putFloat("value", entry.getValue());
            list.add(entryTag);
        }
        root.put("library", list);
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        library.clear();
        if (nbt.contains("library", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("library", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                library.put(entry.getString("key"), entry.getFloat("value"));
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == AffixLibraryCapability.CAPABILITY) {
            return holder.cast();
        }
        return LazyOptional.empty();
    }
}