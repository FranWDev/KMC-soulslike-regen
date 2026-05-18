package dev.kmc.soulslikeregen.capability;

import javax.annotation.Nonnull;

public interface IRegenCap {

    float getCurrentFatigue();
    void setCurrentFatigue(float value);

    float getMaxCap();
    void setMaxCap(float value);

    float getTotalFatigueSpent();
    void addFatigueSpent(float amount);

    int getCurrentLevel();
    void setCurrentLevel(int level);

    long getLastDamageTick();
    void setLastDamageTick(long tick);

    boolean isBonusClaimed();
    void setBonusClaimed(boolean claimed);

    long getLastCampfireUseTick();
    void setLastCampfireUseTick(long tick);

    long getLastBedUseTick();
    void setLastBedUseTick(long tick);

    int getCampfireTicks();
    void setCampfireTicks(int ticks);

    int getExhaustedMessageCooldown();
    void setExhaustedMessageCooldown(int ticks);

    boolean isNexoDrainActive();
    void setNexoDrainActive(boolean active);

    int getInnWarmupTicks();
    void setInnWarmupTicks(int ticks);

    boolean isInnDrainActive();
    void setInnDrainActive(boolean active);

    float getLastKnownHealth();
    void setLastKnownHealth(float health);

    /** Returns true when currentFatigue >= maxCap (healing is blocked). */
    default boolean isExhausted() {
        return getCurrentFatigue() >= getMaxCap();
    }

    /** Adds fatigue and clamps to maxCap. Returns the actual amount added. */
    default float addFatigue(float amount) {
        float before = getCurrentFatigue();
        float capped = Math.min(before + amount, getMaxCap());
        setCurrentFatigue(capped);
        float actual = capped - before;
        if (actual > 0) addFatigueSpent(actual);
        return actual;
    }

    /** Drains fatigue (reduces currentFatigue). Clamps to 0. Returns amount drained. */
    default float drainFatigue(float amount) {
        float before = getCurrentFatigue();
        float drained = Math.min(before, amount);
        setCurrentFatigue(before - drained);
        return drained;
    }
}
