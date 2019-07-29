package com.ioc

import android.content.res.AssetManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by sergeygolishnikov on 09/08/2017.
 */

val keep = "android.support.annotation.Keep"
val nonNull = "android.support.annotation.NonNull"
val iosProvider = IocProvider::class.java.canonicalName.toString()
val iocLazy = IocLazy::class.java.canonicalName.toString()
val iocLocalScope = LocalScope::class.java.canonicalName.toString()
val inject = Inject::class.java.canonicalName.toString()
val assetManager = AssetManager::class.java.canonicalName.toString()
val dependency = Dependency::class.java.canonicalName.toString()
val fragmentActivityPackage = "androidx.fragment.app"
val lifecyclePackage = "androidx.lifecycle"
val androidViewModel = "$lifecyclePackage.ViewModel"
val viewModelProvider = "$lifecyclePackage.ViewModelProvider"
val viewModelProviders = "$lifecyclePackage.ViewModelProviders"
val singleton = Singleton::class.java.canonicalName.toString()

interface BaseTest {
    fun Class<*>.import() : String {
        return "import $name;"
    }
}