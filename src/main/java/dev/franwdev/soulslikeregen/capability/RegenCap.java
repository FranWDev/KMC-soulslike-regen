package dev.franwdev.soulslikeregen.capability;

import dev.franwdev.soulslikeregen.config.RegenConfig;
import net.minecraft.nbt.CompoundTag;

public class RegenCap implements IRegenCap {

    private float currentFatigue = 0.0f;
    private float maxCap;             // initialized to RegenConfig.BASE_MAX_CAP on first access
    private float totalFatigueSpent = 0.0f;
    private int   currentLevel = 0;   // 0 means "base level, no level-up yet"
    private long  lastDamageTick = -1L;
    private boolean bonusClaimed = false;
    // Separate cooldown timestamps per rest source so they are independent
    private long  lastCampfireUseTick = -1L;
    private long  lastBedUseTick = -1L;
    private long  lastWaystoneUseTick = -1L;
    // ── ActionBar Toggle (persisted) ───────────────────────────────────────
    private boolean actionBarEnabled = false;  // default: admin bar display off

    // ── Transient fields (not persisted) ────────────────────────────────────
    private int   campfireTicks = 0;
    private int   exhaustedMessageCooldown = 0;
    private boolean nexoDrainActive = false;
    private int   innWarmupTicks = 0;
    private boolean innDrainActive = false;
    private float lastKnownHealth = -1.0f;

    public RegenCap() {
        // maxCap is set from config at construction time.
        // If config is not baked yet (early init), it defaults to 40.0.
        this.maxCap = RegenConfig.BASE_MAX_CAP > 0 ? RegenConfig.BASE_MAX_CAP : 80.0f;
    }

    // ── IRegenCap implementation ─────────────────────────────────────────────

    @Override public float getCurrentFatigue() { return currentFatigue; }
    @Override public void setCurrentFatigue(float v) { currentFatigue = Math.max(0, v); }

    @Override public float getMaxCap() { return maxCap; }
    @Override public void setMaxCap(float v) { maxCap = Math.max(0, v); }

    @Override public float getTotalFatigueSpent() { return totalFatigueSpent; }
    @Override public void addFatigueSpent(float amount) { totalFatigueSpent += amount; }

    @Override public int getCurrentLevel() { return currentLevel; }
    @Override public void setCurrentLevel(int level) { currentLevel = level; }

    @Override public long getLastDamageTick() { return lastDamageTick; }
    @Override public void setLastDamageTick(long tick) { lastDamageTick = tick; }

    @Override public boolean isBonusClaimed() { return bonusClaimed; }
    @Override public void setBonusClaimed(boolean claimed) { bonusClaimed = claimed; }

    // Campfire and Bed cooldowns are independent and persisted
    @Override public long getLastCampfireUseTick() { return lastCampfireUseTick; }
    @Override public void setLastCampfireUseTick(long tick) { lastCampfireUseTick = tick; }

    @Override public long getLastBedUseTick() { return lastBedUseTick; }
    @Override public void setLastBedUseTick(long tick) { lastBedUseTick = tick; }

    @Override public long getLastWaystoneUseTick() { return lastWaystoneUseTick; }
    @Override public void setLastWaystoneUseTick(long tick) { lastWaystoneUseTick = tick; }

    @Override public int getCampfireTicks() { return campfireTicks; }
    @Override public void setCampfireTicks(int ticks) { campfireTicks = Math.max(0, ticks); }

    @Override public int getExhaustedMessageCooldown() { return exhaustedMessageCooldown; }
    @Override public void setExhaustedMessageCooldown(int ticks) { exhaustedMessageCooldown = Math.max(0, ticks); }

    @Override public boolean isNexoDrainActive() { return nexoDrainActive; }
    @Override public void setNexoDrainActive(boolean active) { nexoDrainActive = active; }

    @Override public int getInnWarmupTicks() { return innWarmupTicks; }
    @Override public void setInnWarmupTicks(int ticks) { innWarmupTicks = Math.max(0, ticks); }

    @Override public boolean isInnDrainActive() { return innDrainActive; }
    @Override public void setInnDrainActive(boolean active) { innDrainActive = active; }

    @Override public float getLastKnownHealth() { return lastKnownHealth; }
    @Override public void setLastKnownHealth(float health) { lastKnownHealth = health; }

    @Override public boolean isActionBarEnabled() { return actionBarEnabled; }
    @Override public void setActionBarEnabled(boolean enabled) { actionBarEnabled = enabled; }

    // ── NBT Persistence ──────────────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("currentFatigue",       currentFatigue);
        tag.putFloat("maxCap",               maxCap);
        tag.putFloat("totalFatigueSpent",    totalFatigueSpent);
        tag.putInt("currentLevel",           currentLevel);
        tag.putLong("lastDamageTick",        lastDamageTick);
        tag.putBoolean("bonusClaimed",       bonusClaimed);
        tag.putLong("lastCampfireUseTick",   lastCampfireUseTick);
        tag.putLong("lastBedUseTick",        lastBedUseTick);
        tag.putLong("lastWaystoneUseTick",   lastWaystoneUseTick);
        tag.putBoolean("actionBarEnabled",   actionBarEnabled);
        // campfireTicks and all other transient fields are intentionally NOT persisted.
        // Zone state resets on restart; losing a few seconds of campfire warmup is acceptable.
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        currentFatigue       = tag.getFloat("currentFatigue");
        maxCap               = tag.contains("maxCap") ? tag.getFloat("maxCap") : RegenConfig.BASE_MAX_CAP;
        totalFatigueSpent    = tag.getFloat("totalFatigueSpent");
        currentLevel         = tag.getInt("currentLevel");
        lastDamageTick       = tag.getLong("lastDamageTick");
        bonusClaimed         = tag.getBoolean("bonusClaimed");
        lastCampfireUseTick  = tag.contains("lastCampfireUseTick") ? tag.getLong("lastCampfireUseTick") : -1L;
        lastBedUseTick       = tag.contains("lastBedUseTick")      ? tag.getLong("lastBedUseTick")      : -1L;
        lastWaystoneUseTick  = tag.contains("lastWaystoneUseTick") ? tag.getLong("lastWaystoneUseTick") : -1L;
        actionBarEnabled     = tag.contains("actionBarEnabled")    ? tag.getBoolean("actionBarEnabled")    : false;
    }
}
