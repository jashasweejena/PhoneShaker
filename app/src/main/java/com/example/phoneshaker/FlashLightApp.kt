package com.example.phoneshaker

import android.app.Application
import com.example.phoneshaker.di.ApplicationCompositionRoot
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.util.DataStoreConstants
import com.example.phoneshaker.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlashLightApp : Application() {
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate() {
        super.onCreate()
        ApplicationCompositionRoot.setup(this)
        preferenceManager = ApplicationCompositionRoot.getPreferenceManager()
        CoroutineScope(Dispatchers.IO).launch {
            val isServiceEnabled =
                preferenceManager.getPrimitive(DataStoreConstants.IS_SERVICE_ENABLED) ?: false
            if (isServiceEnabled) {
                FlashLightService.start(this@FlashLightApp)
            }
        }
    }
}