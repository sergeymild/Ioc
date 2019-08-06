package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject
import javax.inject.Singleton

class BrowserUi

interface Au3
interface Au2

interface AuI: Au3

@Singleton
@Dependency
class AutoCompleteListener: AuI, Au2

@Singleton
class AutoCompleteListenerImpl

class D

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var d: D

    @Inject
    lateinit var autoCompleteListener: AutoCompleteListenerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SingletonFactory.provide(AuI::class.java)


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