package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.common.CommonActivity
import com.example.common.Preferences
import com.example.mylibrary.MyLibraryActivity
import com.ios.injector.R




//interface Test {
//    companion object {
//        @Inject
//        lateinit var myLibraryActivity: MyLibraryActivity
//        init {
//            Ioc.inject(this)
//        }
//    }
//}
//
//
//interface Test2 {
//    companion object {
//        @Inject
//        lateinit var myLibraryActivity: MyLibraryActivity
//        init {
//            Ioc.inject(this)
//        }
//    }
//}

class MainActivity : AppCompatActivity() {

    companion object {
        @Inject
        lateinit var myLibraryActivity: MyLibraryActivity
        init {
            Ioc.inject(this)
        }
    }


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