package com.ioc

import com.example.mylibrary.MyLibraryActivity

open class AlohaBrowserUi

@Dependency
class BrowserUi: AlohaBrowserUi()

class FullscreenWebVideoManager(val BrowserUi: AlohaBrowserUi)

interface BrowserUiCallback

@Dependency
class BrowserUiCallbackImplementation(val FullscreenWebVideoManager: FullscreenWebVideoManager): BrowserUiCallback

@InjectParentDependencies
class MainActivity : MyLibraryActivity() {

//    @Inject
//    @LocalScope
//    var alohaBrowserUi: AlohaBrowserUi? = null
//
//    @Inject
//    lateinit var browserUiCallbackImplementation: BrowserUiCallback
}