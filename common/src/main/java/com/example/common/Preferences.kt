package com.example.common

import androidx.appcompat.app.AppCompatActivity
import com.ioc.Dependency
import javax.inject.Inject

open class CommonActivity: AppCompatActivity() {
    @Inject
    lateinit var prefs: Preferences
}

interface Preferences {

}

@Dependency
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