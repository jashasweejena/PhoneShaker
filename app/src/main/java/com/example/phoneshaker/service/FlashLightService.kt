package com.example.phoneshaker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.phoneshaker.R
import com.example.phoneshaker.util.ChopStateMachine
import com.example.phoneshaker.util.isPhoneInPocket
import com.example.phoneshaker.util.showToast
import kotlin.math.abs

class FlashLightService : Service(), SensorEventListener {
    companion object {
        private val pitchBounds = 10f..90f
        private const val CHANNEL_ID = "FlashlightServiceChannel"
        private var isServiceRunning = false

        fun start(context: Context) {
            if (!isServiceRunning) {
                val serviceIntent = Intent(context, FlashLightService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                isServiceRunning = true
            }
        }

        fun stop(context: Context) {
            if (isServiceRunning) {
                val serviceIntent = Intent(context, FlashLightService::class.java)
                context.stopService(serviceIntent)
                isServiceRunning = false
            }
        }
    }

    private lateinit var sensorManager: SensorManager
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var currentPitchDeg = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        showToast("Service Started")
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

    private fun isUpright(): Boolean {
        return abs(currentPitchDeg) in pitchBounds
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor?.type != Sensor.TYPE_LINEAR_ACCELERATION) return
        if (isPhoneInPocket() || !isUpright()) {
            return
        }
        val acceleration = event.values[0]
        ChopStateMachine.onChop(
            now = SystemClock.elapsedRealtime(),
            acceleration = acceleration,
            context = this
        )
    }

    private val rotationListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            currentPitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    private fun setupSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let { rotSensor ->
            sensorManager.registerListener(
                rotationListener,
                rotSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        showToast("Service Destroyed")
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }
}