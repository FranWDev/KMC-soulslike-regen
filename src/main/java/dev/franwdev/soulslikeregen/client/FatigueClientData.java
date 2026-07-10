package dev.franwdev.soulslikeregen.client;

public class FatigueClientData {

    public enum RecoveryType {
        NONE,
        NEXUS,
        REST
    }

    private static volatile float currentFatigue = 0f;
    private static volatile float maxCap = 1f;
    private static volatile boolean exhausted = false;
    private static volatile RecoveryType recoveryType = RecoveryType.NONE;

    public static void update(float fatigue, float max, boolean isExhausted, RecoveryType type) {
        currentFatigue = fatigue;
        maxCap = max;
        exhausted = isExhausted;
        recoveryType = type;
    }

    public static float getCurrentFatigue() {
        return currentFatigue;
    }

    public static float getMaxCap() {
        return maxCap;
    }

    public static boolean isExhausted() {
        return exhausted;
    }

    public static RecoveryType getRecoveryType() {
        return recoveryType;
    }
}
