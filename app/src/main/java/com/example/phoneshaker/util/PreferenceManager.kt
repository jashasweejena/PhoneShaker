package com.example.phoneshaker.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

interface PreferenceManager {
    suspend fun <T> savePrimitive(key: DataStoreKey<T>, value: T)
    suspend fun <T> getPrimitive(key: DataStoreKey<T>): T?
    suspend fun <T> removePrimitive(key: DataStoreKey<T>)
    fun <T> getPrimitiveFlow(key: DataStoreKey<T>): Flow<T?>
    fun <T> getPrimitiveFlow(key: DataStoreKey<T>, defaultValue: T): Flow<T>
    suspend fun clearAll()
}

class PreferenceManagerImpl(private val context: Context) : PreferenceManager {
    override suspend fun <T> savePrimitive(key: DataStoreKey<T>, value: T) {
        context.datastore.edit {
            it[key.preferenceKey] = value
        }
    }

    override suspend fun <T> getPrimitive(key: DataStoreKey<T>): T? {
        return context.datastore.data.firstOrNull()?.get(key.preferenceKey)
    }

    override suspend fun <T> removePrimitive(key: DataStoreKey<T>) {
        context.datastore.edit {
            it.remove(key.preferenceKey)
        }
    }

    override fun <T> getPrimitiveFlow(key: DataStoreKey<T>): Flow<T?> {
        return context.datastore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[key.preferenceKey]
            }
    }

    override fun <T> getPrimitiveFlow(key: DataStoreKey<T>, defaultValue: T): Flow<T> {
        return getPrimitiveFlow(key).map { it ?: defaultValue }
    }

    override suspend fun clearAll() {
        kotlin.runCatching {
            context.datastore.edit {
                it.clear()
            }
        }
    }
}