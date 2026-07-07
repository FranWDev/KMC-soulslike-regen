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
import java.util.Optional;
import java.util.List;

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

    public NexusEntry upsertNexus(double x, double y, double z, double radius, ResourceKey<Level> dimension, UUID teamId, String teamName) {
        Optional<NexusEntry> existing = getNexusByTeamName(teamName);
        if (existing.isPresent()) {
            int id = existing.get().id();
            NexusEntry entry = new NexusEntry(id, x, y, z, radius, dimension, teamId, teamName);
            nexuses.put(id, entry);
            setDirty();
            return entry;
        } else {
            return addNexus(x, y, z, radius, dimension, teamId, teamName);
        }
    }

    public boolean removeNexusByTeamName(String teamName) {
        Optional<NexusEntry> opt = getNexusByTeamName(teamName);
        if (opt.isPresent()) {
            nexuses.remove(opt.get().id());
            setDirty();
            return true;
        }
        return false;
    }

    public boolean updateNexusRadiusByTeamName(String teamName, double radius) {
        Optional<NexusEntry> opt = getNexusByTeamName(teamName);
        if (opt.isPresent()) {
            NexusEntry old = opt.get();
            nexuses.put(old.id(), new NexusEntry(old.id(), old.x(), old.y(), old.z(), radius, old.dimension(), old.teamId(), old.teamName()));
            setDirty();
            return true;
        }
        return false;
    }

    public boolean updateNexusCoordsByTeamName(String teamName, double x, double y, double z) {
        Optional<NexusEntry> opt = getNexusByTeamName(teamName);
        if (opt.isPresent()) {
            NexusEntry old = opt.get();
            nexuses.put(old.id(), new NexusEntry(old.id(), x, y, z, old.radius(), old.dimension(), old.teamId(), old.teamName()));
            setDirty();
            return true;
        }
        return false;
    }

    public Optional<NexusEntry> getNexusByTeamName(String teamName) {
        return nexuses.values().stream()
            .filter(n -> n.teamName().equals(teamName))
            .findFirst();
    }

    public List<String> getAllNexusTeamNames() {
        return nexuses.values().stream()
            .map(NexusEntry::teamName)
            .toList();
    }

    public Collection<NexusEntry> getAllNexuses() {
        return nexuses.values();
    }
}
