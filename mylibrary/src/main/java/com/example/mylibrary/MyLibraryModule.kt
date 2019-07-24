package com.example.mylibrary

import com.example.common.*
import com.ioc.Dependency
import com.ioc.LibraryModules



object BottomModule {
//    @Dependency
//    @JvmStatic
    fun factory(preferences: Preferences, configProvider: ApplicationConfigProvider): BottomFactory {
        return BottomAdHolderFactory(preferences, configProvider)
    }
}
