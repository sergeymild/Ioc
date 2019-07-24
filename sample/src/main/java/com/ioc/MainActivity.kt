package com.ioc

import android.app.Activity
import com.example.common.Preferences
import com.example.mylibrary.MyLibraryActivity
import javax.inject.Inject


class MainActivity : Activity() {

    @Inject
    lateinit var preferences: Preferences
}