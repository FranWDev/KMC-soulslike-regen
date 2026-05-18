package dev.franwdev.soulslikeregen.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import dev.franwdev.soulslikeregen.capability.RegenCap;

/**
 * JUnit tests for fatigue calculation and clamping logic.
 */
public class FatigueCalculationTest {

    private RegenCap cap;

    @BeforeEach
    void setup() {
        cap = new RegenCap();
    }

    @Test
    void testAddFatigueClamps() {
        cap.setMaxCap(10.0f);
        float added = cap.addFatigue(15.0f);
        assertEquals(10.0f, cap.getCurrentFatigue(), 0.01f);
        assertEquals(10.0f, added, 0.01f, "Only 10 should be added");
    }

    @Test
    void testDrainFatigue() {
        cap.setCurrentFatigue(10.0f);
        float drained = cap.drainFatigue(3.0f);
        assertEquals(3.0f, drained, 0.01f);
        assertEquals(7.0f, cap.getCurrentFatigue(), 0.01f);
    }

    @Test
    void testDrainBeyondZero() {
        cap.setCurrentFatigue(2.0f);
        float drained = cap.drainFatigue(10.0f);
        assertEquals(2.0f, drained, 0.01f, "Only 2 available");
        assertEquals(0.0f, cap.getCurrentFatigue(), 0.01f);
    }

    @Test
    void testIsExhausted() {
        cap.setMaxCap(10.0f);
        cap.setCurrentFatigue(9.9f);
        assertFalse(cap.isExhausted(), "9.9/10 should not be exhausted");

        cap.setCurrentFatigue(10.0f);
        assertTrue(cap.isExhausted(), "10.0/10 should be exhausted");

        cap.setCurrentFatigue(10.1f);  // Will be clamped to 10.0
        assertTrue(cap.isExhausted(), "10.1/10 (clamped) should be exhausted");
    }

    @Test
    void testFatigueNeverNegative() {
        cap.setCurrentFatigue(-5.0f);
        assertEquals(0.0f, cap.getCurrentFatigue(), 0.01f, "Negative fatigue should be clamped to 0");
    }

    @Test
    void testFatigueNeverExceedsMax() {
        cap.setMaxCap(20.0f);
        cap.addFatigue(100.0f);
        assertEquals(20.0f, cap.getCurrentFatigue(), 0.01f, "Fatigue cannot exceed max");
    }

    @Test
    void testTotalFatigueSpentAccumulates() {
        float before = cap.getTotalFatigueSpent();
        cap.addFatigue(5.0f);
        float after = cap.getTotalFatigueSpent();
        assertTrue(after >= before, "Total fatigue spent should accumulate");
        assertTrue(after - before >= 4.9f, "Should accumulate at least 5.0");
    }

    @Test
    void testZeroDrain() {
        cap.setCurrentFatigue(10.0f);
        float drained = cap.drainFatigue(0.0f);
        assertEquals(0.0f, drained, 0.01f);
        assertEquals(10.0f, cap.getCurrentFatigue(), 0.01f);
    }

    @Test
    void testZeroAdd() {
        cap.setCurrentFatigue(5.0f);
        float added = cap.addFatigue(0.0f);
        assertEquals(0.0f, added, 0.01f);
        assertEquals(5.0f, cap.getCurrentFatigue(), 0.01f);
    }
}
