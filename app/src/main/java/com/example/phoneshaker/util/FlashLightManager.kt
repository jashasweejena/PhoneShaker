package com.example.phoneshaker.util

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager

object FlashLightManager {
    private var flashLightStatus: Boolean = false
    fun toggleFlashLight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        if (!flashLightStatus) {
            kotlin.runCatching {
                cameraManager.setTorchMode(cameraId, true)
                flashLightStatus = true
            }
        } else {
            kotlin.runCatching {
                cameraManager.setTorchMode(cameraId, false)
                flashLightStatus = false
            }
        }

    }
}