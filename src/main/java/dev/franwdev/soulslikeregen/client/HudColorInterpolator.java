package dev.franwdev.soulslikeregen.client;

import dev.franwdev.soulslikeregen.client.FatigueClientData.RecoveryType;

public class HudColorInterpolator {
    private float r = 255f;
    private float g = 255f;
    private float b = 255f;
    private long lastTimeMs = 0L;

    public int getColor(boolean exhausted, RecoveryType recoveryType) {
        long now = System.currentTimeMillis();
        float dt = (lastTimeMs == 0) ? 0f : (now - lastTimeMs) / 1000f;
        lastTimeMs = now;

        double time = now / 1000.0;
        // 2 seconds per cycle (0.5 Hz)
        float pulseFactor = (float) (Math.sin(time * Math.PI) + 1.0) / 2.0f;

        float targetR, targetG, targetB;

        if (exhausted) {
            // Pulse between bright red (255, 0, 0) and dark red (136, 0, 0)
            targetR = 136f + (255f - 136f) * pulseFactor;
            targetG = 0f;
            targetB = 0f;
        } else {
            switch (recoveryType) {
                case NEXUS -> {
                    // Pulse light blue: (80, 170, 255) to (130, 230, 255)
                    targetR = 80f + 50f * pulseFactor;
                    targetG = 170f + 60f * pulseFactor;
                    targetB = 255f;
                }
                case REST -> {
                    // Pulse green: (34, 170, 34) to (85, 255, 85)
                    targetR = 34f + 51f * pulseFactor;
                    targetG = 170f + 85f * pulseFactor;
                    targetB = 34f + 51f * pulseFactor;
                }
                default -> {
                    // White
                    targetR = 255f;
                    targetG = 255f;
                    targetB = 255f;
                }
            }
        }

        // Smooth transition using exponential decay (lerp)
        float lerpSpeed = 4.0f;
        float alpha = Math.min(1.0f, dt * lerpSpeed);
        r = r + (targetR - r) * alpha;
        g = g + (targetG - g) * alpha;
        b = b + (targetB - b) * alpha;

        int ir = Math.min(255, Math.max(0, Math.round(r)));
        int ig = Math.min(255, Math.max(0, Math.round(g)));
        int ib = Math.min(255, Math.max(0, Math.round(b)));

        return 0xFF000000 | (ir << 16) | (ig << 8) | ib;
    }
}
