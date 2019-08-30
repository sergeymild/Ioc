package com.ioc.common

import com.ioc.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import java.lang.ref.WeakReference

val lifecyclePackage = "androidx.lifecycle"
val iocLazyType = ClassName.get(IocLazy::class.java)
val iocProviderType = ClassName.get(IocProvider::class.java)
val weakType = ClassName.get(WeakReference::class.java)
val viewModelFactoryType = ClassName.bestGuess("$lifecyclePackage.ViewModelProvider.Factory")
val keepAnnotation = ClassName.bestGuess("androidx.annotation.Keep")
val nonNullAnnotation = ClassName.bestGuess("androidx.annotation.NonNull")
val nullableAnnotation = ClassName.bestGuess("androidx.annotation.Nullable")
val viewModelProvidersType = ClassName.bestGuess("$lifecyclePackage.ViewModelProviders")
val viewModelType = ClassName.bestGuess("$lifecyclePackage.ViewModel")
val androidLiveDataObserver = ClassName.bestGuess("$lifecyclePackage.Observer")
val hashMapType = ClassName.get(HashMap::class.java)
val weakReferenceType = ClassName.get(WeakReference::class.java)
val lifecycleOwner = "androidx.lifecycle.LifecycleOwner"
val javaClassType = ParameterizedTypeName.get(ClassName.bestGuess("java.lang.Class"), TypeVariableName.get("T"))
val scanJavaType = Scan::class.java
val injectJavaType = Inject::class.java
val localScopeJavaType = LocalScope::class.java
val singletonJavaType = Singleton::class.java
val weakJavaType = WeakReference::class.java
val lazyJavaType = Lazy::class.java
val providerJavaType = Provider::class.java
val viewModelJavaType = ViewModel::class.java
val objectJavaType = Object::class.java

val androidXFragmentPackage = "androidx.fragment.app.Fragment"
val viewModelPackages = listOf("android.arch.lifecycle.ViewModel", "androidx.lifecycle.ViewModel")
val liveDataPackages = listOf("androidx.lifecycle.MutableLiveData", "androidx.lifecycle.LiveData")
val allowedViewModelParents = listOf(
    "androidx.fragment.app.Fragment",
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
    "android.view",
    "androidx"
)

fun provideMethodName(model: DependencyModel): String {
    return "provide${model.originalType.simpleName.capitalize()}"
}

fun targetInjectionClassName(target: TargetType): String {
    return "${target.name}Injector"
}

fun targetInjectionPackage(target: TargetType): String {
    return target.className.packageName()
}

fun targetInjectionTypeName(target: TargetType): TypeName {
    return ClassName.bestGuess("${targetInjectionPackage(target)}.${targetInjectionClassName(target)}")
}

fun singletonClassName(model: DependencyModel): String {
    return "${model.originalType.simpleName.capitalize()}Singleton"
}

fun singletonClassPackage(model: DependencyModel): String {
    val name = model.originalType.getPackage().toString()
    if (excludedPackages.any { name.startsWith(it) }) return "com.ioc"
    return name
}

fun singletonTypeName(model: DependencyModel): TypeName {
    return ClassName.bestGuess("${singletonClassPackage(model)}.${singletonClassName(model)}")
}

fun namedStringForError(named: String?): String {
    return if (named.isNullOrBlank()) "@Default" else "@$named"
}