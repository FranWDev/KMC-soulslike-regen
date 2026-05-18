package dev.kmc.soulslikeregen.compat;

import net.minecraftforge.fml.ModList;

public class FTBTeamsCompat {
    public static boolean isLoaded() {
        return ModList.get().isLoaded("ftbteams");
    }

    public static void init() {
        // Stub for Phase 1
    }
}
