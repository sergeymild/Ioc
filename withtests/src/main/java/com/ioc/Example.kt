package com.ioc


interface ApplicationConfigProvider {
    fun provideConfig(): String
}

object Module {
    @Dependency
    @JvmStatic
    fun provideApplicationConfigProvider() = object : ApplicationConfigProvider {
        override fun provideConfig() = "str"
    }
}

class Example {

}