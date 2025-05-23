package com.example.phoneshaker.util

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.datastore by preferencesDataStore("flash_light_preferences")
