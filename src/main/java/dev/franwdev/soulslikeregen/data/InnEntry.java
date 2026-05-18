package dev.franwdev.soulslikeregen.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record InnEntry(
    int id,
    double x,
    double y,
    double z,
    double radius,
    ResourceKey<Level> dimension
) {
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putDouble("radius", radius);
        tag.putString("dimension", dimension.location().toString());
        return tag;
    }

    public static InnEntry fromNBT(CompoundTag tag) {
        int id = tag.getInt("id");
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");
        double radius = tag.getDouble("radius");
        ResourceKey<Level> dim = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(tag.getString("dimension"))
        );
        return new InnEntry(id, x, y, z, radius, dim);
    }
}
