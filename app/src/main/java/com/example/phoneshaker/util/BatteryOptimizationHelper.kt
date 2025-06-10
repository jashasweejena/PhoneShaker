package com.example.phoneshaker.util

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.net.toUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BatteryOptimizationHelper {
    fun initialize()
    fun isIgnoringBatteryOptimizations(): Boolean
    fun getBatteryOptimizationStatus(): StateFlow<BatteryOptimizationState>
    fun requestIgnoreBatteryOptimizations()
}

class BatteryOptimizationHelperImpl(private val activity: ComponentActivity) :
    BatteryOptimizationHelper {

    // Activity result launcher for handling the battery optimization request
    private lateinit var batteryOptimizationLauncher: ActivityResultLauncher<Intent>
    private val batteryOptimizationStatus =
        if (isIgnoringBatteryOptimizations()) MutableStateFlow(BatteryOptimizationState.GRANTED) else MutableStateFlow(
            BatteryOptimizationState.DENIED
        )

    override fun initialize() {
        batteryOptimizationLauncher = activity.registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handle the result if needed
            if (isIgnoringBatteryOptimizations()) {
                // App is now whitelisted
                onBatteryOptimizationGranted()
            } else {
                // User declined or app is still not whitelisted
                onBatteryOptimizationDenied()
            }
        }
    }

    /**
     * Check if the app is currently ignoring battery optimizations
     */
    override fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }

    override fun getBatteryOptimizationStatus(): StateFlow<BatteryOptimizationState> {
        return batteryOptimizationStatus.asStateFlow()
    }

    /**
     * Show the ignore battery optimization dialog
     */
    override fun requestIgnoreBatteryOptimizations() {
        if (!isIgnoringBatteryOptimizations()) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:${activity.packageName}".toUri()
            }

            // Check if the intent can be resolved
            if (intent.resolveActivity(activity.packageManager) != null) {
                batteryOptimizationLauncher.launch(intent)
            } else {
                // If the specific intent doesn't work, open battery optimization settings
                openBatteryOptimizationSettings()
            }
        } else {
            // Already whitelisted
            onBatteryOptimizationGranted()
        }
    }

    /**
     * Open battery optimization settings page as fallback
     */
    private fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings if specific settings are not available
            val intent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        }
    }

    /**
     * Called when battery optimization is granted/already granted
     */
    private fun onBatteryOptimizationGranted() {
        // Handle success - app is now whitelisted
        // You can show a success message or continue with your app logic
        batteryOptimizationStatus.update {
            BatteryOptimizationState.GRANTED
        }
    }

    /**
     * Called when battery optimization is denied
     */
    private fun onBatteryOptimizationDenied() {
        // Handle denial - you might want to explain why this permission is needed
        // or continue without it
        batteryOptimizationStatus.update {
            BatteryOptimizationState.DENIED
        }
    }
}

enum class BatteryOptimizationState {
    GRANTED, DENIED
}