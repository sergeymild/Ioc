package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.ios.injector.R
import javax.inject.Inject
import javax.inject.Singleton

class MyViewModel: ViewModel() {
    val liveData = MutableLiveData<String>()
}


class Fra: Fragment() {
    @Inject
    var myViewModel: MyViewModel? = null

    @DataObserver
    fun dataObserver(string: String) {

    }
}


//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


//    @Inject
//    lateinit var myViewModel: MyViewModel
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