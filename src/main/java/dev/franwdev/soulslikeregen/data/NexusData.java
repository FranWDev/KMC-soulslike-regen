package dev.franwdev.soulslikeregen.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexusData extends SavedData {
    private static final String FILE_NAME = "soulslikeregen_nexus";

    private final Map<Integer, NexusEntry> nexuses = new HashMap<>();
    private int nextId = 1;

    public static NexusData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            NexusData::load,
            NexusData::new,
            FILE_NAME
        );
    }

    public NexusData() {}

    public static NexusData load(CompoundTag tag) {
        NexusData data = new NexusData();
        data.nextId = tag.getInt("nextId");
        if (tag.contains("nexuses", Tag.TAG_LIST)) {
            ListTag list = tag.getList("nexuses", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                NexusEntry entry = NexusEntry.fromNBT(list.getCompound(i));
                data.nexuses.put(entry.id(), entry);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("nextId", nextId);
        ListTag list = new ListTag();
        for (NexusEntry entry : nexuses.values()) {
            list.add(entry.toNBT());
        }
        tag.put("nexuses", list);
        return tag;
    }

    public NexusEntry addNexus(double x, double y, double z, double radius, ResourceKey<Level> dimension, UUID teamId, String teamName) {
        int id = nextId++;
        NexusEntry entry = new NexusEntry(id, x, y, z, radius, dimension, teamId, teamName);
        nexuses.put(id, entry);
        setDirty();
        return entry;
    }

    public boolean removeNexus(int id) {
        if (nexuses.containsKey(id)) {
            nexuses.remove(id);
            setDirty();
            return true;
        }
        return false;
    }

    public NexusEntry getNexus(int id) {
        return nexuses.get(id);
    }

    public Collection<NexusEntry> getAllNexuses() {
        return nexuses.values();
    }
}
