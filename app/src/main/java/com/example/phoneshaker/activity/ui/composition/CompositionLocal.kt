package com.example.phoneshaker.activity.ui.composition

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.phoneshaker.util.BatteryOptimizationHelper

val LocalBatteryHelper = staticCompositionLocalOf<BatteryOptimizationHelper> {
    error("BatteryOptimizationHelper not provided")
}