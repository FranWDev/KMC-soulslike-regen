package dev.franwdev.soulslikeregen.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record NexusEntry(
    int id,
    double x,
    double y,
    double z,
    double radius,
    ResourceKey<Level> dimension,
    UUID teamId,
    String teamName
) {
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putDouble("radius", radius);
        tag.putString("dimension", dimension.location().toString());
        tag.putUUID("teamId", teamId);
        tag.putString("teamName", teamName);
        return tag;
    }

    public static NexusEntry fromNBT(CompoundTag tag) {
        int id = tag.getInt("id");
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");
        double radius = tag.getDouble("radius");
        ResourceKey<Level> dim = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(tag.getString("dimension"))
        );
        UUID teamId = tag.getUUID("teamId");
        String teamName = tag.getString("teamName");
        return new NexusEntry(id, x, y, z, radius, dim, teamId, teamName);
    }
}
