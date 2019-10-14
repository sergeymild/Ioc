package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.download.media.views.speeddial.models.SpeedDialModel
import com.example.mylibrary.BottomAdHolderFactory
import com.example.mylibrary.Library
import com.ios.injector.R

class M


@Module(value = [Library::class])
object MainModule {
    @Dependency
    @Singleton
    fun sd() = SpeedDialModel()
}





class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var bottomAdHolderFactory: BottomAdHolderFactory
    @Inject
    lateinit var speedDialModel: SpeedDialModel
    @Inject
    lateinit var m: M


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