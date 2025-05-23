package com.example.phoneshaker.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProximitySensorUtils private constructor(private val context: Context) : SensorEventListener {
    
    companion object {
        @Volatile
        private var INSTANCE: ProximitySensorUtils? = null
        
        fun getInstance(context: Context): ProximitySensorUtils {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProximitySensorUtils(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    
    private var proximityMaxRange: Float = 0f
    private var isListening = false
    
    // StateFlow to observe proximity changes
    private val _isInPocket = MutableStateFlow(false)
    val isInPocket: StateFlow<Boolean> = _isInPocket.asStateFlow()
    
    // Callback interface for direct callbacks
    interface ProximityCallback {
        fun onProximityChanged(isInPocket: Boolean)
    }
    
    private var callback: ProximityCallback? = null
    
    init {
        proximitySensor?.let {
            proximityMaxRange = it.maximumRange
        }
    }
    
    /**
     * Start listening to proximity sensor changes
     * @param callback Optional callback for immediate updates
     */
    fun startListening(callback: ProximityCallback? = null) {
        if (proximitySensor == null) {
            // No proximity sensor available
            _isInPocket.value = false
            callback?.onProximityChanged(false)
            return
        }
        
        if (!isListening) {
            this.callback = callback
            sensorManager.registerListener(
                this,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            isListening = true
        }
    }
    
    /**
     * Stop listening to proximity sensor changes
     */
    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            callback = null
        }
    }
    
    /**
     * Get current pocket status (only valid if currently listening)
     */
    fun getCurrentStatus(): Boolean {
        return _isInPocket.value
    }
    
    /**
     * Check if proximity sensor is available on device
     */
    fun isProximitySensorAvailable(): Boolean {
        return proximitySensor != null
    }
    
    /**
     * Get proximity sensor maximum range
     */
    fun getMaxRange(): Float {
        return proximityMaxRange
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val inPocket = determineIfInPocket(distance)
            
            // Only update if state changed
            if (_isInPocket.value != inPocket) {
                _isInPocket.value = inPocket
                callback?.onProximityChanged(inPocket)
            }
        }
    }
    
    private fun determineIfInPocket(distance: Float): Boolean {
        return when {
            // Very close to sensor (definitely covered)
            distance <= 0.1f -> true
            
            // Close to sensor but not touching (probably in pocket/bag)
            distance < proximityMaxRange * 0.15f -> true
            
            // Far from sensor (definitely not covered)
            distance >= proximityMaxRange * 0.8f -> false
            
            // Ambiguous distance - maintain previous state to avoid flickering
            else -> _isInPocket.value
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
}

// Extension functions for easier usage
fun Context.getProximitySensorUtils(): ProximitySensorUtils {
    return ProximitySensorUtils.getInstance(this)
}

// Simple one-shot check (requires active listening)
fun Context.isPhoneInPocket(): Boolean {
    val proximityUtils = getProximitySensorUtils()
    return if (proximityUtils.isProximitySensorAvailable()) {
        proximityUtils.getCurrentStatus()
    } else {
        false // No sensor available, assume not in pocket
    }
}