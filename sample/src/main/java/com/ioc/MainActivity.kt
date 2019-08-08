package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R


class MainActivity : AppCompatActivity() {

    @set:DebugLog("callAnother")
    var stringRepresentation: String
        get() = this.toString()
        set(value) {}

    fun callAnother() {
        println("Was called callAnother")
    }

    lateinit var _s: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stringRepresentation = "d"

        var s = "ds"




//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}