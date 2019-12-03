package com.ioc

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import javax.inject.Inject

abstract class Mo {

    companion object {
        @Dependency
        fun pr() = object: ApplicationConfigProvider {
            override fun provideConfig(): String {
                return "str2"
            }
        }
    }
}


class ExampleUnitTest {

    @Inject
    lateinit var provider: ApplicationConfigProvider

    @Before
    fun init() {
        Ioc.inject(this)
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(provider.provideConfig(), "str2")
    }
}
