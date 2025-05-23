package com.example.phoneshaker

import android.app.Application
import com.example.phoneshaker.di.appModule
import com.example.phoneshaker.util.PreferenceManager
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
    }

    private fun setupDi() {
        startKoin {
            androidLogger()
            androidContext(this@FlashLightApp)
            modules(appModule)
        }
    }
}