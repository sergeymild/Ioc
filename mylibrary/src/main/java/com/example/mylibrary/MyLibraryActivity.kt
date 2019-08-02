package com.example.mylibrary

import android.app.Activity
import javax.inject.Inject

open class MyLibraryActivity : Activity() {
    @Inject
    lateinit var bottomFactory: BottomFactory
}