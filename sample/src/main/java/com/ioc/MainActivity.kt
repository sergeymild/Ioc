package com.ioc

import com.example.mylibrary.MyLibraryActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Single

interface AlohaBrowserUi

@Dependency
class BrowserUi(val single: Single): AlohaBrowserUi

class FullscreenWebVideoManager(val BrowserUi: AlohaBrowserUi, val single: Single)

interface BrowserUiCallback

@Dependency
class BrowserUiCallbackImplementation(val FullscreenWebVideoManager: FullscreenWebVideoManager, val single: Single): BrowserUiCallback

//@InjectParentDependencies
class MainActivity : MyLibraryActivity() {

    @Inject
    var alohaBrowserUi: AlohaBrowserUi? = null

    @Inject
    lateinit var browserUiCallbackImplementation: BrowserUiCallback
}