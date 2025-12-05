package com.zybooks.inspobook.util

import org.junit.Assert.*
import org.junit.Test

class ShakeDetectorTest {

    @Test
    fun `below threshold does not trigger shake`() {
        val detector = ShakeDetector(threshold = 18f, cooldownMs = 1000L)

        val triggered = detector.onAcceleration(gForce = 10f, timestampMs = 1000L)

        assertFalse("Shake should not trigger when below threshold", triggered)
    }

    @Test
    fun `above threshold triggers shake`() {
        val detector = ShakeDetector(threshold = 18f, cooldownMs = 1000L)

        val triggered = detector.onAcceleration(gForce = 20f, timestampMs = 1000L)

        assertTrue("Shake should trigger when above threshold", triggered)
    }

    @Test
    fun `cooldown prevents immediate second trigger`() {
        val detector = ShakeDetector(threshold = 18f, cooldownMs = 1000L)

        val first = detector.onAcceleration(gForce = 20f, timestampMs = 1000L)
        val second = detector.onAcceleration(gForce = 20f, timestampMs = 1500L) // only 500 ms later

        assertTrue("First shake should trigger", first)
        assertFalse("Second shake within cooldown should NOT trigger", second)
    }

    @Test
    fun `shake can trigger again after cooldown`() {
        val detector = ShakeDetector(threshold = 18f, cooldownMs = 1000L)

        val first = detector.onAcceleration(gForce = 20f, timestampMs = 1000L)
        val second = detector.onAcceleration(gForce = 20f, timestampMs = 2500L) // 1500 ms later

        assertTrue("First shake should trigger", first)
        assertTrue("Second shake after cooldown should trigger again", second)
    }
}
