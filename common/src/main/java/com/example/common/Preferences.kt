package com.example.common

import com.ioc.Dependency


interface Preferences {

}

class PreferencesInt: Preferences {

}



interface ApplicationContextProvider
interface ApplicationConfigProvider

class ApplicationConfigProviderImpl: ApplicationConfigProvider {

}

interface CommonModule {
    @Dependency
    fun provideConfig(impl: ApplicationConfigProviderImpl): ApplicationConfigProvider
}