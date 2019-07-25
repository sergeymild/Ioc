package com.ioc

import android.support.v7.app.AppCompatActivity
import javax.inject.Inject

class AlohaBrowserUi

class AutoCompleteListenerImpl(val browserUi: AlohaBrowserUi)

class AddressBarListenerImpl(@field:LocalScope val browserUi: AlohaBrowserUi) {
    @Inject
    lateinit var listener: AutoCompleteListenerImpl
}

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


}