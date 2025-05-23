package com.example.phoneshaker.di

import com.example.phoneshaker.util.PreferenceManager
import com.example.phoneshaker.util.PreferenceManagerImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::PreferenceManagerImpl) bind PreferenceManager::class
}