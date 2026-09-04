package dev.franwdev.soulslikeregen.network;

import dev.franwdev.soulslikeregen.client.FatigueClientData.RecoveryType;

public class FatigueDataPacket {
    private final float currentFatigue;
    private final float maxCap;
    private final boolean exhausted;
    private final RecoveryType recoveryType;

    public FatigueDataPacket(float currentFatigue, float maxCap, boolean exhausted, RecoveryType recoveryType) {
        this.currentFatigue = currentFatigue;
        this.maxCap = maxCap;
        this.exhausted = exhausted;
        this.recoveryType = recoveryType;
    }

    public float getCurrentFatigue() {
        return currentFatigue;
    }

    public float getMaxCap() {
        return maxCap;
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public RecoveryType getRecoveryType() {
        return recoveryType;
    }

    public FatigueDataPayload toPayload() {
        return new FatigueDataPayload(currentFatigue, maxCap, exhausted, recoveryType);
    }
}
