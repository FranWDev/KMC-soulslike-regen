package dev.franwdev.soulslikeregen.compat;

import net.minecraftforge.fml.ModList;

public class WaystonesCompat {
    public static boolean isLoaded() {
        return ModList.get().isLoaded("waystones");
    }

    public static void init() {
        // Stub for Phase 1
    }
}
