package com.zybooks.inspobook.util

/**
 * Shake detector for the random picture feature
 *
 * Returns true when a shake should be triggered
 */
class ShakeDetector(
    private val threshold: Float = 18f,
    private val cooldownMs: Long = 1000L
) {
    private var lastShakeTime: Long? = null

    fun onAcceleration(gForce: Float, timestampMs: Long): Boolean {
        // not enough acceleration → not a 'strong' shake
        if (gForce <= threshold) return false

        val last = lastShakeTime

        val canTrigger = if (last == null) {
            true
        } else {
            (timestampMs - last) >= cooldownMs
        }

        return if (canTrigger) {
            lastShakeTime = timestampMs
            true
        } else {
            false
        }
    }
}
