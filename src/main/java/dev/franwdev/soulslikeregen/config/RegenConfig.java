package dev.franwdev.soulslikeregen.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegenConfig {

    public static final ForgeConfigSpec SPEC;

    // ── Baked static fields (read once, used in hot paths) ──────────────────

    /** Starting capacity in half-hearts (e.g. 80 = 40 hearts). */
    public static float BASE_MAX_CAP = 80.0f;

    /** Ally proximity bonus reduction per ally (default 0.1 = 10%). */
    public static float ALLY_DISCOUNT_PER_PLAYER = 0.10f;

    /** Maximum ally discount cap (default 0.5 = 50%). */
    public static float ALLY_DISCOUNT_MAX = 0.50f;

    /** Radius in blocks to scan for allies. */
    public static double ALLY_SCAN_RADIUS = 16.0;

    /** Nexus drain rate: units of fatigue drained per interval. */
    public static float NEXUS_DRAIN_RATE = 1.0f;

    /** Nexus drain interval: ticks between each drain tick (default 20 = 1 second). */
    public static int NEXUS_DRAIN_INTERVAL_TICKS = 20;

    /** Inn warmup ticks before drain starts (20 ticks/s × 60s = 1200 ticks). */
    public static int INN_WARMUP_TICKS = 1200;

    /** Inn drain rate: units of fatigue drained per interval. */
    public static float INN_DRAIN_RATE = 1.0f;

    /** Inn drain interval: ticks between each drain tick (default 40 = 2 seconds). */
    public static int INN_DRAIN_INTERVAL_TICKS = 40;

    /** Campfire rest ticks required (30 seconds = 600 ticks). */
    public static int CAMPFIRE_REQUIRED_TICKS = 600;

    /** Campfire fatigue reduction per use. */
    public static float CAMPFIRE_REDUCTION = 20.0f;

    /** Ticks between campfire rest uses (default 24000 = 1 in-game day). */
    public static int CAMPFIRE_COOLDOWN_TICKS = 24000;

    /** Bed rest fatigue reduction per sleep. */
    public static float BED_REDUCTION = 20.0f;

    /** Ticks between bed rest uses (default 24000 = 1 in-game day). */
    public static int BED_COOLDOWN_TICKS = 24000;

    /** Day survival bonus (24000 ticks without damage). */
    public static float DAY_BONUS_REDUCTION = 40.0f;

    /** Ordered list of level definitions (populated in bake()). */
    public static List<LevelDefinition> LEVELS = new ArrayList<>();

    // ── Config spec entries ──────────────────────────────────────────────────

    private static final ForgeConfigSpec.DoubleValue CFG_BASE_MAX_CAP;
    private static final ForgeConfigSpec.DoubleValue CFG_ALLY_DISCOUNT_PER_PLAYER;
    private static final ForgeConfigSpec.DoubleValue CFG_ALLY_DISCOUNT_MAX;
    private static final ForgeConfigSpec.DoubleValue CFG_ALLY_SCAN_RADIUS;
    private static final ForgeConfigSpec.DoubleValue CFG_NEXUS_DRAIN_RATE;
    private static final ForgeConfigSpec.IntValue    CFG_NEXUS_DRAIN_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue    CFG_INN_WARMUP_TICKS;
    private static final ForgeConfigSpec.DoubleValue CFG_INN_DRAIN_RATE;
    private static final ForgeConfigSpec.IntValue    CFG_INN_DRAIN_INTERVAL_TICKS;
    private static final ForgeConfigSpec.IntValue    CFG_CAMPFIRE_REQUIRED_TICKS;
    private static final ForgeConfigSpec.DoubleValue CFG_CAMPFIRE_REDUCTION;
    private static final ForgeConfigSpec.IntValue    CFG_CAMPFIRE_COOLDOWN_TICKS;
    private static final ForgeConfigSpec.DoubleValue CFG_BED_REDUCTION;
    private static final ForgeConfigSpec.IntValue    CFG_BED_COOLDOWN_TICKS;
    private static final ForgeConfigSpec.DoubleValue CFG_DAY_BONUS_REDUCTION;
    private static final ForgeConfigSpec.IntValue    CFG_LEVEL_COUNT;

    // Level entries: indexed by level number (1-based)
    private static final List<ForgeConfigSpec.DoubleValue> CFG_LEVEL_CAPACITY_INCREASE = new ArrayList<>();
    private static final List<ForgeConfigSpec.DoubleValue> CFG_LEVEL_FATIGUE_THRESHOLD = new ArrayList<>();

    /**
     * The number of levels defined in config. The mod ships with 3 default levels.
     * Admins can increase this and add new [level_N] sections.
     */
    private static final int DEFAULT_LEVEL_COUNT = 3;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("KMC Soulslike Regen — Common Configuration").push("general");

        CFG_BASE_MAX_CAP = b
            .comment("Starting regeneration capacity in half-hearts (80 = 40 hearts).")
            .defineInRange("base_max_cap", 80.0, 1.0, 10000.0);

        CFG_ALLY_DISCOUNT_PER_PLAYER = b
            .comment("Fatigue cost reduction per nearby ally (0.1 = 10%).")
            .defineInRange("ally_discount_per_player", 0.10, 0.0, 1.0);

        CFG_ALLY_DISCOUNT_MAX = b
            .comment("Maximum total ally discount (0.5 = 50%).")
            .defineInRange("ally_discount_max", 0.50, 0.0, 1.0);

        CFG_ALLY_SCAN_RADIUS = b
            .comment("Radius in blocks to search for teammates.")
            .defineInRange("ally_scan_radius", 16.0, 1.0, 128.0);

        CFG_NEXUS_DRAIN_RATE = b
            .comment("Fatigue units drained per interval inside a Nexus.")
            .defineInRange("nexus_drain_rate", 1.0, 0.01, 100.0);

        CFG_NEXUS_DRAIN_INTERVAL_TICKS = b
            .comment("Ticks between each Nexus drain tick (20 = 1s, 40 = 2s, 80 = 4s).")
            .defineInRange("nexus_drain_interval_ticks", 20, 1, 72000);

        CFG_INN_WARMUP_TICKS = b
            .comment("Ticks the player must stay in an Inn before recovery starts (1200 = 60s).")
            .defineInRange("inn_warmup_ticks", 1200, 0, 72000);

        CFG_INN_DRAIN_RATE = b
            .comment("Fatigue units drained per interval inside an Inn.")
            .defineInRange("inn_drain_rate", 1.0, 0.01, 100.0);

        CFG_INN_DRAIN_INTERVAL_TICKS = b
            .comment("Ticks between each Inn drain tick (40 = 2s, 20 = 1s, 100 = 5s).")
            .defineInRange("inn_drain_interval_ticks", 40, 1, 72000);

        CFG_CAMPFIRE_REQUIRED_TICKS = b
            .comment("Ticks near a lit campfire to trigger rest (600 = 30s).")
            .defineInRange("campfire_required_ticks", 600, 20, 72000);

        CFG_CAMPFIRE_REDUCTION = b
            .comment("Fatigue units removed by a campfire rest.")
            .defineInRange("campfire_reduction", 20.0, 0.0, 10000.0);

        CFG_CAMPFIRE_COOLDOWN_TICKS = b
            .comment("Ticks between campfire rest uses (24000 = 1 in-game day, 12000 = half day).")
            .defineInRange("campfire_cooldown_ticks", 24000, 20, 2_000_000);

        CFG_BED_REDUCTION = b
            .comment("Fatigue units removed by sleeping in a bed.")
            .defineInRange("bed_reduction", 20.0, 0.0, 10000.0);

        CFG_BED_COOLDOWN_TICKS = b
            .comment("Ticks between bed rest uses (24000 = 1 in-game day).")
            .defineInRange("bed_cooldown_ticks", 24000, 20, 2_000_000);

        CFG_DAY_BONUS_REDUCTION = b
            .comment("Fatigue units removed after surviving 24000 ticks without damage.")
            .defineInRange("day_bonus_reduction", 40.0, 0.0, 10000.0);

        b.pop();

        // ── Level System ────────────────────────────────────────────────────
        b.comment("Level system: define how maxCap grows as the player spends fatigue.").push("levels");

        CFG_LEVEL_COUNT = b
            .comment("Number of levels defined below. Add matching [levels.level_N] sections.")
            .defineInRange("level_count", DEFAULT_LEVEL_COUNT, 0, 100);

        for (int i = 1; i <= 20; i++) {
            // Pre-register up to 20 levels; only level_count of them will be read in bake()
            b.push("level_" + i);

            double defaultIncrease = switch (i) {
                case 1 -> 20.0;
                case 2 -> 20.0;
                case 3 -> 30.0;
                default -> 10.0;
            };
            double defaultThreshold = switch (i) {
                case 1 -> 400.0;
                case 2 -> 1400.0;
                case 3 -> 3000.0;
                default -> 3000.0 + (i - 3) * 2000.0;
            };

            CFG_LEVEL_CAPACITY_INCREASE.add(b
                .comment("maxCap increase (in half-hearts) when this level is reached.")
                .defineInRange("capacity_increase", defaultIncrease, 0.01, 10000.0));

            CFG_LEVEL_FATIGUE_THRESHOLD.add(b
                .comment("Cumulative total fatigue spent (never resets) required to reach this level.")
                .defineInRange("fatigue_threshold", defaultThreshold, 1.0, 1_000_000.0));

            b.pop();
        }

        b.pop();
        SPEC = b.build();
    }

    /** Call once after config is loaded (e.g. in FMLCommonSetupEvent or ModConfigEvent.Reloading). */
    public static void bake() {
        BASE_MAX_CAP                = (float) CFG_BASE_MAX_CAP.get().doubleValue();
        ALLY_DISCOUNT_PER_PLAYER    = (float) CFG_ALLY_DISCOUNT_PER_PLAYER.get().doubleValue();
        ALLY_DISCOUNT_MAX           = (float) CFG_ALLY_DISCOUNT_MAX.get().doubleValue();
        ALLY_SCAN_RADIUS            = CFG_ALLY_SCAN_RADIUS.get();
        NEXUS_DRAIN_RATE            = (float) CFG_NEXUS_DRAIN_RATE.get().doubleValue();
        NEXUS_DRAIN_INTERVAL_TICKS  = CFG_NEXUS_DRAIN_INTERVAL_TICKS.get();
        INN_WARMUP_TICKS            = CFG_INN_WARMUP_TICKS.get();
        INN_DRAIN_RATE              = (float) CFG_INN_DRAIN_RATE.get().doubleValue();
        INN_DRAIN_INTERVAL_TICKS    = CFG_INN_DRAIN_INTERVAL_TICKS.get();
        CAMPFIRE_REQUIRED_TICKS     = CFG_CAMPFIRE_REQUIRED_TICKS.get();
        CAMPFIRE_REDUCTION          = (float) CFG_CAMPFIRE_REDUCTION.get().doubleValue();
        CAMPFIRE_COOLDOWN_TICKS     = CFG_CAMPFIRE_COOLDOWN_TICKS.get();
        BED_REDUCTION               = (float) CFG_BED_REDUCTION.get().doubleValue();
        BED_COOLDOWN_TICKS          = CFG_BED_COOLDOWN_TICKS.get();
        DAY_BONUS_REDUCTION         = (float) CFG_DAY_BONUS_REDUCTION.get().doubleValue();

        int count = CFG_LEVEL_COUNT.get();
        LEVELS = new ArrayList<>(count);
        for (int i = 0; i < count && i < CFG_LEVEL_CAPACITY_INCREASE.size(); i++) {
            float increase  = (float) CFG_LEVEL_CAPACITY_INCREASE.get(i).get().doubleValue();
            float threshold = (float) CFG_LEVEL_FATIGUE_THRESHOLD.get(i).get().doubleValue();
            LEVELS.add(new LevelDefinition(i + 1, increase, threshold));
        }
        LEVELS = Collections.unmodifiableList(LEVELS);
    }

    /** Immutable snapshot of one level's definition. */
    public record LevelDefinition(int level, float capacityIncrease, float fatigueThreshold) {}
}
