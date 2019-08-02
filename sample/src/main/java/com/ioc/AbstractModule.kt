package com.ioc

import com.example.mylibrary.BottomAdHolderFactory
import com.example.mylibrary.BottomFactory

abstract class AbstractModule {
    @Dependency
    abstract fun bottomFactory(factory: BottomAdHolderFactory): BottomFactory
}