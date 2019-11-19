package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.download.media.views.speeddial.models.SpeedDialModel
import com.download.transitionlibrary.UpdateManager
import com.example.mylibrary.BottomAdHolderFactory
import com.example.mylibrary.Library
import com.ios.injector.R



@Singleton
class S: Cleanable {
    override fun onCleared() {
        println("s1---")
    }

}

@Singleton
class S2: Cleanable {
    override fun onCleared() {
        println("s2---")
    }

    companion object {
        init {
            println("s2 -=======")
        }
    }
}

class S22 {
    @Inject
    lateinit var s: S2
}

class MainActivity : AppCompatActivity() {




    @Inject
    lateinit var s2: TestCl




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(TestCl::class.java)
        println(S2::class.java)
        println("after touch .java")
        Ioc.inject(this)
        println("after inject")
//        Ioc.clearSingletons(true)
//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}