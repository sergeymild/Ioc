package com.ioc

import javax.inject.Inject

object Module {
    @Dependency
    fun provideExampleDependencyString(): String {
        return "example"
    }
}

@Dependency
class TestParent : IParent {

    init {
        Ioc.inject(this)
    }

    @Inject
    lateinit var exampleString: String

    override fun s(): String {
        return exampleString
    }

}