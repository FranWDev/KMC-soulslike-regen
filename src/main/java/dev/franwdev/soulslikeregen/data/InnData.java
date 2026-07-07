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
import java.util.Optional;
import java.util.List;

public class InnData extends SavedData {
    private static final String FILE_NAME = "soulslikeregen_inn";

    private final Map<Integer, InnEntry> inns = new HashMap<>();
    private int nextId = 1;

    public static InnData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            InnData::load,
            InnData::new,
            FILE_NAME
        );
    }

    public InnData() {}

    public static InnData load(CompoundTag tag) {
        InnData data = new InnData();
        data.nextId = tag.getInt("nextId");
        if (tag.contains("inns", Tag.TAG_LIST)) {
            ListTag list = tag.getList("inns", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                InnEntry entry = InnEntry.fromNBT(list.getCompound(i));
                data.inns.put(entry.id(), entry);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("nextId", nextId);
        ListTag list = new ListTag();
        for (InnEntry entry : inns.values()) {
            list.add(entry.toNBT());
        }
        tag.put("inns", list);
        return tag;
    }

    public InnEntry addInn(String name, double x, double y, double z, double radius, ResourceKey<Level> dimension) {
        int id = nextId++;
        InnEntry entry = new InnEntry(id, name, x, y, z, radius, dimension);
        inns.put(id, entry);
        setDirty();
        return entry;
    }

    public boolean removeInnByName(String name) {
        Optional<InnEntry> opt = getInnByName(name);
        if (opt.isPresent()) {
            inns.remove(opt.get().id());
            setDirty();
            return true;
        }
        return false;
    }

    public boolean updateInnRadiusByName(String name, double radius) {
        Optional<InnEntry> opt = getInnByName(name);
        if (opt.isPresent()) {
            InnEntry old = opt.get();
            inns.put(old.id(), new InnEntry(old.id(), old.name(), old.x(), old.y(), old.z(), radius, old.dimension()));
            setDirty();
            return true;
        }
        return false;
    }

    public boolean updateInnCoordsByName(String name, double x, double y, double z) {
        Optional<InnEntry> opt = getInnByName(name);
        if (opt.isPresent()) {
            InnEntry old = opt.get();
            inns.put(old.id(), new InnEntry(old.id(), old.name(), x, y, z, old.radius(), old.dimension()));
            setDirty();
            return true;
        }
        return false;
    }

    public Optional<InnEntry> getInnByName(String name) {
        return inns.values().stream()
            .filter(inn -> inn.name().equals(name))
            .findFirst();
    }

    public boolean hasInnWithName(String name) {
        return inns.values().stream()
            .anyMatch(inn -> inn.name().equals(name));
    }

    public List<String> getAllInnNames() {
        return inns.values().stream()
            .map(InnEntry::name)
            .toList();
    }

    public Collection<InnEntry> getAllInns() {
        return inns.values();
    }
}
