package com.ioc


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject

abstract class RootModule {
    abstract fun getStr()

    object NestedModule {
        @Dependency
        @JvmStatic
        fun getString() = "strin"
    }
}

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var string: String


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
}