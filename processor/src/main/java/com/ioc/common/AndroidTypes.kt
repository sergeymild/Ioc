package com.ioc.common

val viewModelPackages = listOf("android.arch.lifecycle.ViewModel", "androidx.lifecycle.ViewModel")
val allowedViewModelParents = listOf(
    "android.support.v4.app.Fragment",
    "android.support.v4.app.FragmentActivity",
    "androidx.appcompat.app.AppCompatActivity"
)