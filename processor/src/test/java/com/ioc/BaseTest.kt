package com.ioc

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.lang.ref.WeakReference

/**
 * Created by sergeygolishnikov on 09/08/2017.
 */

val keep = "android.support.annotation.Keep"
val nonNull = "android.support.annotation.NonNull"
val ioc = Ioc::class.java.canonicalName.toString()
val iosProvider = IocProvider::class.java.canonicalName.toString()
val iocLazy = IocLazy::class.java.canonicalName.toString()
val iocLocalScope = LocalScope::class.java.canonicalName.toString()
val named = Qualifier::class.java.canonicalName.toString()

val importKotlinMetadataAnnotation = "import ${Metadata::class.java.canonicalName};"

val importAndroidContext = "import ${Context::class.java.canonicalName};"
val importAndroidActivity = "import ${Activity::class.java.canonicalName};"
val importAndroidSharedPreferences = "import ${SharedPreferences::class.java.canonicalName};"

val importWeakReference = "import ${WeakReference::class.java.canonicalName};"
val importList = "import ${List::class.java.canonicalName};"

val importRxJavaCompositeDisposable = "import ${CompositeDisposable::class.java.canonicalName};"
val importRxJavaSubject = "import ${Subject::class.java.canonicalName};"
val importRxJavaBehaviorSubject = "import ${BehaviorSubject::class.java.canonicalName};"


val importProvider = "import ${Provider::class.java.canonicalName};"
val importIocProvider = "import ${IocProvider::class.java.canonicalName};"
val importLazy = "import ${Lazy::class.java.canonicalName};"
val importIocLazy = "import ${IocLazy::class.java.canonicalName};"

val importInjectAnnotation = "import ${Inject::class.java.canonicalName};"
val importSingletonAnnotation = "import ${Singleton::class.java.canonicalName};"
val importDependencyAnnotation = "import ${Dependency::class.java.canonicalName};"
val importLocalScopeAnnotation = "import ${LocalScope::class.java.canonicalName};"
val importPostInitializationAnnotation = "import ${PostInitialization::class.java.canonicalName};"
val importKeepAnnotation = "import androidx.annotation.Keep;"
val importNonNullAnnotation = "import androidx.annotation.NonNull;"
val importQualifierAnnotation = "import ${Qualifier::class.java.canonicalName};"
val importViewModelAnnotation = "import ${ViewModel::class.java.canonicalName};"

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