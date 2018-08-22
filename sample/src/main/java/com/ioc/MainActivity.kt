package com.ioc

import android.support.v7.app.AppCompatActivity
import javax.inject.Inject
import javax.inject.Singleton


class LazyDep(val dep: Dep)

@Singleton
class Dep

class Parent(val lazyDep: Lazy<LazyDep>, val dep: Dep)

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var parent: Parent

}