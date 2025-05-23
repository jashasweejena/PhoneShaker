package com.example.phoneshaker

import android.app.Application
import com.example.phoneshaker.di.appModule
import com.example.phoneshaker.service.FlashLightService
import com.example.phoneshaker.util.DataStoreConstants
import com.example.phoneshaker.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin

class FlashLightApp : Application(), KoinComponent {
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate() {
        super.onCreate()
        setupDi()
        preferenceManager = get<PreferenceManager>()
        startServiceIfNeeded()
    }

    private fun startServiceIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val isServiceEnabled =
                preferenceManager.getPrimitive(DataStoreConstants.IS_SERVICE_ENABLED) ?: false
            if (isServiceEnabled) {
                FlashLightService.start(this@FlashLightApp)
            }
        }
    }

    private fun setupDi() {
        startKoin {
            androidLogger()
            androidContext(this@FlashLightApp)
            modules(appModule)
        }
    }
}