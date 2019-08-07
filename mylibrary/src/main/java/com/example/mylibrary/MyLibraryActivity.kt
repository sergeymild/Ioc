package com.example.mylibrary

import com.ioc.Ioc
import javax.inject.Inject

open class MyLibraryActivity {
    @Inject
    lateinit var bottomFactory: BottomFactory

    init {
        Ioc.inject(this)
    }
}