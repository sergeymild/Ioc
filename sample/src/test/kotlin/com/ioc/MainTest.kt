package com.ioc

import android.os.Build
import org.junit.Assert
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MainTest {

    @Inject
    lateinit var parent: IParent

    @Before
    fun setup() {
        Ioc.inject(this)
    }

    @Test
    fun `01 - TopSitesService returns correct data`() {
        Assert.assertTrue(parent.s() == "example")
    }
}