package com.example.phoneshaker.util

import android.content.Context
import kotlin.math.abs

object ChopStateMachine {
    var ACCELERATION_THRESHOLD = 40L
    private const val SHAKE_SLOP_TIME_MS = 700L
    private const val COOLDOWN_PERIOD = 700L


    private var chopState = ChopState.IDLE
    private var firstHitTimeMs = 0L
    private var cooldownTimeMs = 0L
    private var armed = false
    fun onChop(now: Long, acceleration: Float, context: Context) {
        if (!armed && abs(acceleration) > ACCELERATION_THRESHOLD) {
            armed = true
            registerChop(now, context)
        }

        if (armed && acceleration > 40f) {
            armed = false
        }

        if (chopState == ChopState.COOLDOWN && (now - cooldownTimeMs) > COOLDOWN_PERIOD) {
            chopState = ChopState.IDLE
        }

        if (chopState == ChopState.IDLE && (now - firstHitTimeMs) > SHAKE_SLOP_TIME_MS) {
            chopState = ChopState.IDLE
        }
    }

    private fun registerChop(now: Long, context: Context) {
        when (chopState) {
            ChopState.IDLE -> {
                firstHitTimeMs = now
                chopState = ChopState.FIRST_HIT
            }

            ChopState.FIRST_HIT -> {
                if (now - firstHitTimeMs <= SHAKE_SLOP_TIME_MS) {
                    FlashLightManager.toggleFlashLight(context)
                    chopState = ChopState.COOLDOWN
                    cooldownTimeMs = now
                } else {
                    firstHitTimeMs = now            // treat as new first chop
                }
            }

            ChopState.COOLDOWN -> Unit
        }
    }
}

private enum class ChopState {
    IDLE, COOLDOWN, FIRST_HIT
}