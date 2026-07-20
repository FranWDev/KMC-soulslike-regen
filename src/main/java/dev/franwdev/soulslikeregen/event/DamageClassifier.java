package dev.franwdev.soulslikeregen.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Classifies damage sources to identify environmental damage vs combat damage.
 * Environmental damage yields a reduced rate of fatigue buildup when healed.
 */
public final class DamageClassifier {

    private DamageClassifier() {}

    /**
     * Checks if a damage source is environmental.
     * Must have no causing entity and no direct/projectile entity,
     * and match one of the common environmental tags or explicit type IDs.
     */
    public static boolean isEnvironmental(DamageSource source) {
        // If there's an attacking entity or direct projectile, it's combat/PvE
        if (source.getEntity() != null) return false;
        if (source.getDirectEntity() != null) return false;

        // Check Forge/Vanilla category tags
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypeTags.IS_LIGHTNING)) {
            return true;
        }

        // Handle specific non-tagged environmental sources via their registry name
        ResourceLocation id = source.typeHolder().unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        if (id != null) {
            String path = id.getPath();
            return "cactus".equals(path)
                    || "in_wall".equals(path)
                    || "cramming".equals(path)
                    || "dryout".equals(path)
                    || "fly_into_wall".equals(path)
                    || "hot_floor".equals(path)
                    || "magic".equals(path)
                    || "outside_border".equals(path)
                    || "fall_outside_world".equals(path);
        }

        return false;
    }
}
