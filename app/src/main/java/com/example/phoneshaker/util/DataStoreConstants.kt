package com.example.phoneshaker.util

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

data class DataStoreKey<T>(val preferenceKey: Preferences.Key<T>)

object DataStoreConstants {
    val IS_SERVICE_ENABLED = DataStoreKey(booleanPreferencesKey("is_service_enabled"))
}

