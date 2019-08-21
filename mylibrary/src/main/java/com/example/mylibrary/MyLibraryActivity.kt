package com.example.mylibrary

import com.ioc.Inject
import com.ioc.Ioc


open class MyLibraryActivity {
    @Inject
    lateinit var bottomFactory: BottomFactory

    init {
        Ioc.inject(this)
    }
}