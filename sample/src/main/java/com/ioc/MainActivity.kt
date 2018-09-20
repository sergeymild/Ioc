package com.ioc

import android.support.v7.app.AppCompatActivity
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton

@PerActivity
class Dep

@Scope
annotation class PerActivity


@ScopeRoot
@PerActivity
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var holder: PresenterHolder
}

class PresenterHolder {
    @Inject
    lateinit var presenter: Presenter
}

class Presenter(@PerActivity val dep: Dep)