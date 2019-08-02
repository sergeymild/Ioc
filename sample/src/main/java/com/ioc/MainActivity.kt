package com.ioc


import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

interface Dep

class DepImpl: Dep

object Module {
    @Dependency
    fun provide(): Dep {
        return DepImpl()
    }
}


//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var dep: Dep
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//
//
////        try {
////            Class.forName("androidx.test.espresso.Espresso")
////            findViewById<TextView>(R.id.test_text).setText("Test")
////        } catch (e: ClassNotFoundException) {
////        }
////
//
//    }
//
//    @DataObserver
//    fun dataObserver(string: String) {
//
//    }
}