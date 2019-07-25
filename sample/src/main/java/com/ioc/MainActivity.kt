package com.ioc

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import javax.inject.Inject

open class BrowserUi

class AlohaBrowserUi(val s: S): BrowserUi() {

}

interface AutoCompleteListener

@Dependency
class AutoCompleteListenerImpl(val browserUi: AlohaBrowserUi): AutoCompleteListener

class AutocompleteController(private var autoCompleteListener: AutoCompleteListener)


class S

class AddressBarListenerImpl(@field:LocalScope val browserUi: AlohaBrowserUi) {
    @Inject
    lateinit var autocompleteController: AutocompleteController
}

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


    @Inject
    @LocalScope
    lateinit var alohaBrowserUi: AlohaBrowserUi
}