package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.download.transitionlibrary.PublicTraDe
import com.example.mylibrary.MyLibraryActivity
import com.ios.injector.R
import javax.inject.Inject

object Module {
    @Dependency
    fun provideExampleDependencyString(): String {
        return "example"
    }
}

interface IParent {
    fun s(): String
}

@Dependency
class Parent: IParent {

    @Inject
    lateinit var exampleString : String

    override fun s(): String {
        return exampleString
    }

    init {
        Ioc.inject(this)
    }
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



//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}