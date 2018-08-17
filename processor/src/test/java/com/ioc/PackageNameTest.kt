package com.ioc

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */
@RunWith(JUnit4::class)
public class PackageNameTest {
    @Test
    @Throws(Exception::class)
    fun testPackages() {
        val packages = setOf("com.ioc.browser.presentation.main", "com.ioc.browser.presentation.main.launcher")
        Assert.assertEquals(PackageResolver.resolve(packages), "com.ioc.browser.presentation.main")
    }

    @Test
    @Throws(Exception::class)
    fun testPackages2() {
        val packages = setOf("com.github.io", "com.github.launcher")
        Assert.assertEquals(PackageResolver.resolve(packages), "com.github")
    }
}