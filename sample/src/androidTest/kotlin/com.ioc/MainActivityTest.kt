package com.ioc

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    val activity = ActivityTestRule(MainActivity::class.java, false, false)

    @Before
    fun setup() {
        System.setProperty("IS_UI_TESTING", "true")
        activity.launchActivity(null)
    }

    @Test
    fun runAsExpected() {
        SystemClock.sleep(1000)
    }
}