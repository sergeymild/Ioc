package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject

class BrowserUi

interface AutoCompleteListener

@Dependency
class AutoCompleteListenerImpl(val browserUi: BrowserUi): AutoCompleteListener

open class HistorySearch

class AutocompleteController constructor(
    private var autoCompleteListener: AutoCompleteListener,
    private val historySearch: HistorySearch)

object Module {
    @Dependency
    fun autocompleteController(
        browserUi: BrowserUi,
        listener: AutoCompleteListener,
        historySearch: HistorySearch
    ): AutocompleteController = AutocompleteController(listener, historySearch)

    @Dependency
    fun historySearch(): HistorySearch {
        return object : HistorySearch() {}
    }
}

class AddressBarListenerImpl(@LocalScope val browserUi: BrowserUi) {
    @Inject
    lateinit var autocompleteController: AutocompleteController
}

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
//
//    @DataObserver
//    fun dataObserver(string: String) {
//
//    }
}