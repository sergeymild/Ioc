package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject

class BrowserUi



//@InjectParentDependencies
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var browserUi: Lazy<BrowserUi>

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