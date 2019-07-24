package com.ioc

import com.example.mylibrary.MyLibraryActivity
import javax.inject.Inject

class S

class D @Inject constructor(val s: S) {

}

@ParentDependencies
class MainActivity : MyLibraryActivity() {

    @Inject
    lateinit var d: D
}