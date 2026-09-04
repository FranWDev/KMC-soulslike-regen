package dev.franwdev.soulslikeregen.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SoulslikeRegenClientConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue HUD_X_OFFSET;
    public static final ModConfigSpec.IntValue HUD_Y_OFFSET;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("KMC Soulslike Regen — Client Configuration").push("hud");

        HUD_X_OFFSET = b
            .comment("Horizontal offset of the fatigue HUD overlay from the center of the screen.")
            .defineInRange("x_offset", 0, -1000, 1000);

        HUD_Y_OFFSET = b
            .comment("Vertical offset of the fatigue HUD overlay from the bottom of the screen. Default is -59 (above experience bar).")
            .defineInRange("y_offset", -59, -1000, 0);

        b.pop();
        SPEC = b.build();
    }
}
