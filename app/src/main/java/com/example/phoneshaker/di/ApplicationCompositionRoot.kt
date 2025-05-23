package com.example.phoneshaker.di

import com.example.phoneshaker.FlashLightApp
import com.example.phoneshaker.util.PreferenceManager
import com.example.phoneshaker.util.PreferenceManagerImpl

object ApplicationCompositionRoot {
    private val _preferenceManager: PreferenceManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        PreferenceManagerImpl(app)
    }
    private lateinit var app: FlashLightApp

    fun setup(app: FlashLightApp) {
        this.app = app
    }

    fun getPreferenceManager(): PreferenceManager = _preferenceManager

}