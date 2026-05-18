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

    public InnEntry addInn(double x, double y, double z, double radius, ResourceKey<Level> dimension) {
        int id = nextId++;
        InnEntry entry = new InnEntry(id, x, y, z, radius, dimension);
        inns.put(id, entry);
        setDirty();
        return entry;
    }

    public boolean removeInn(int id) {
        if (inns.containsKey(id)) {
            inns.remove(id);
            setDirty();
            return true;
        }
        return false;
    }

    public boolean updateInnRadius(int id, double radius) {
        InnEntry old = inns.get(id);
        if (old != null) {
            inns.put(id, new InnEntry(id, old.x(), old.y(), old.z(), radius, old.dimension()));
            setDirty();
            return true;
        }
        return false;
    }

    public boolean updateInnCoords(int id, double x, double y, double z) {
        InnEntry old = inns.get(id);
        if (old != null) {
            inns.put(id, new InnEntry(id, x, y, z, old.radius(), old.dimension()));
            setDirty();
            return true;
        }
        return false;
    }

    public InnEntry getInn(int id) {
        return inns.get(id);
    }

    public Collection<InnEntry> getAllInns() {
        return inns.values();
    }
}
