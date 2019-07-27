package com.ioc


import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


interface DepI

@Dependency
class Dep(context: Context): DepI

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var lazyDep: Provider<DepI>


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