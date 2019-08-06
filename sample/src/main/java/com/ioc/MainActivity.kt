package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.ios.injector.R
import javax.inject.Inject

class BrowserUi: LifecycleOwner {
    override fun getLifecycle(): Lifecycle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Inject
    @ViewModelDependency
    lateinit var vm: VM

    @DataObserver
    fun ds(string: String) {

    }
}


class VM {
    val dataO = MutableLiveData<String>()
}

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

    @DataObserver
    fun dataObserver(string: String) {

    }
}