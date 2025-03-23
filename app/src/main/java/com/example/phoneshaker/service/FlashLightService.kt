package com.example.phoneshaker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.phoneshaker.R
import com.example.phoneshaker.util.FlashLightManager
import kotlin.math.sqrt

class FlashLightService : Service(), SensorEventListener {
    private companion object {
        const val ACCELERATION_THRESHOLD = 30L
        const val SHAKE_SLOP_TIME_MS = 500L
        const val COOLDOWN_PERIOD = 500L // Cooldown period in milliseconds
        const val CHANNEL_ID = "FlashlightServiceChannel"
    }

    private lateinit var sensorManager: SensorManager

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val alpha = 0.8f // Smoothing factor (adjust as needed)

    private var lastChopTime = 0L
    private var lastGestureTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        setupSensorStuff()
        startForegroundService()
    }

    private fun startForegroundService() {
        // Create notification channel (required for Android 8+)
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flashlight Service")
            .setContentText("Listening for chop-chop gestures")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Flashlight Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = lowPass(event.values[0], lastX)
            val y = lowPass(event.values[1], lastY)
            val z = lowPass(event.values[2], lastZ)

            lastX = x
            lastY = y
            lastZ = z

            val netAcceleration = sqrt(x * x + y * y + z * z)
            val now = System.currentTimeMillis()

            if (now - lastGestureTime >= COOLDOWN_PERIOD) {
                if (netAcceleration >= ACCELERATION_THRESHOLD) {
                    if (now - lastChopTime <= SHAKE_SLOP_TIME_MS) {
                        FlashLightManager.toggleFlashLight(applicationContext)
                        lastGestureTime = now
                    }
                    lastChopTime = now
                }
            }
        }
    }

    private fun setupSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                3,
                3
            )
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    private fun lowPass(current: Float, last: Float): Float {
        return last + alpha * (current - last)
    }
}