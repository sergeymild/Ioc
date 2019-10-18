package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.download.media.views.speeddial.models.SpeedDialModel
import com.example.mylibrary.BottomAdHolderFactory
import com.example.mylibrary.Library
import com.ios.injector.R

interface FirstLo
interface SecondLo

@Dependency
@Singleton
class Logger: FirstLo

@Dependency
fun getSec(firstLo: FirstLo): SecondLo? = null




class MainActivity : AppCompatActivity() {


    @Inject
    @LocalScope
    lateinit var FirstLo: FirstLo

    @Inject
    lateinit var SecondLo: SecondLo




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