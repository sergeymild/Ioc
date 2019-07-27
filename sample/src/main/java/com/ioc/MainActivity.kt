package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject

class Dep

object Module {
    @Dependency
    fun getDependency(s: String) = Dep()

    @Dependency
    fun geString() = "some"
}

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var dep: Dep


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