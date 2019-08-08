package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.download.transitionlibrary.PublicTraDe
import com.example.mylibrary.MyLibraryActivity
import com.ios.injector.R
import javax.inject.Inject

interface Modu {
    @Dependency @Scan
    fun provideMyLibraryActivity(): MyLibraryActivity
    @Dependency @Scan
    fun trad(): PublicTraDe
}

class VM {
    val dataO = MutableLiveData<String>()
}

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var myLibraryActivity: MyLibraryActivity
    @Inject
    lateinit var traDe: PublicTraDe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Ioc.inject(this)

        Fragment


//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}