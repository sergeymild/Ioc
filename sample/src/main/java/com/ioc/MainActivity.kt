package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.download.media.views.speeddial.models.SpeedDialModel
import com.download.transitionlibrary.UpdateManager
import com.example.mylibrary.BottomAdHolderFactory
import com.example.mylibrary.Library
import com.ios.injector.R


object Modu {

    interface Abs {
        @Scan
        @Dependency
        fun updateManager(): UpdateManager
    }
}

class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var updateManager: UpdateManager




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Ioc.inject(this)
//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}