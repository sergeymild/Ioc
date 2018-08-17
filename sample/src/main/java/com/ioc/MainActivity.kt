package com.ioc

import android.support.v7.app.AppCompatActivity
import com.example.common.ApplicationConfigProvider
import com.example.common.ApplicationContextProvider
import com.example.common.CommonModule
import com.example.common.Preferences
import com.example.mylibrary.BottomFactory
import com.example.mylibrary.BottomModule
import com.example.mylibrary.MyLibraryModule
import javax.inject.Inject
import javax.inject.Provider

@Dependency
class ApplicationContextProviderImpl: ApplicationContextProvider {

}





@LibraryModules(BottomModule::class)
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var bottom: BottomFactory
}