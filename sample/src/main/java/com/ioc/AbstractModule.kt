package com.ioc

import com.example.common.Preferences
import com.example.common.PreferencesInt
import javax.inject.Singleton

abstract class AbstractModule {
    @Dependency
    @Singleton
    abstract fun preferences(preferences: PreferencesInt): Preferences
}