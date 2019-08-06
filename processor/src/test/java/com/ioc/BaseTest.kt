package com.ioc

import android.content.res.AssetManager
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by sergeygolishnikov on 09/08/2017.
 */

val keep = "android.support.annotation.Keep"
val nonNull = "android.support.annotation.NonNull"
val ioc = Ioc::class.java.canonicalName.toString()
val iosProvider = IocProvider::class.java.canonicalName.toString()
val iocLazy = IocLazy::class.java.canonicalName.toString()
val iocLocalScope = LocalScope::class.java.canonicalName.toString()
val iocDataObserver = DataObserver::class.java.canonicalName.toString()
val named = Named::class.java.canonicalName.toString()
val inject = Inject::class.java.canonicalName.toString()
val lazyType = Lazy::class.java.canonicalName.toString()
val providerType = Provider::class.java.canonicalName.toString()
val weakReferenceType = WeakReference::class.java.canonicalName.toString()
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