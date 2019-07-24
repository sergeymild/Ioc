package com.ioc

import com.example.mylibrary.MyLibraryActivity
import javax.inject.Inject

class D

@ParentDependencies
class MainActivity : MyLibraryActivity() {

    @Inject
    lateinit var d: D
}