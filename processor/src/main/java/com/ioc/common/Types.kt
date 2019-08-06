package com.ioc.common

import com.ioc.DependencyModel
import com.ioc.Ioc
import com.ioc.IocLazy
import com.ioc.IocProvider
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import java.lang.ref.WeakReference

val lifecyclePackage = "androidx.lifecycle"
val iocLazyType = ClassName.get(IocLazy::class.java)
val iocProviderType = ClassName.get(IocProvider::class.java)
val iocType = ClassName.get(Ioc::class.java)
val weakType = ClassName.get(WeakReference::class.java)
val viewModelFactoryType = ClassName.bestGuess("$lifecyclePackage.ViewModelProvider.Factory")
val keepAnnotation = ClassName.bestGuess("android.support.annotation.Keep")
val nonNullAnnotation = ClassName.bestGuess("android.support.annotation.NonNull")
val viewModelProvidersType = ClassName.bestGuess("$lifecyclePackage.ViewModelProviders")
val viewModelType = ClassName.bestGuess("$lifecyclePackage.ViewModel")
val androidLiveDataObserver = ClassName.bestGuess("$lifecyclePackage.Observer")

val viewModelPackages = listOf("android.arch.lifecycle.ViewModel", "androidx.lifecycle.ViewModel")
val liveDataPackages = listOf("androidx.lifecycle.MutableLiveData", "androidx.lifecycle.LiveData")
val allowedViewModelParents = listOf(
    "android.support.v4.app.Fragment",
    "android.support.v4.app.FragmentActivity",
    "androidx.appcompat.app.AppCompatActivity"
)

val excludedPackages = setOf(
    "java",
    "sun",
    "org.jetbrains",
    "android.content",
    "android.util",
    "android.app",
    "android.view"
)

fun singletonClassName(model: DependencyModel): String {
    return "${model.originalType.simpleName.capitalize()}Singleton"
}

fun singletonClassPackage(model: DependencyModel): String {
    return model.originalType.getPackage().toString()
}

fun singletonTypeName(model: DependencyModel): TypeName {
    return ClassName.bestGuess("${singletonClassPackage(model)}.${singletonClassName(model)}")
}