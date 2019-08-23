package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.download.media.views.speeddial.models.SpeedDialModel
import com.download.media.views.speeddial.viewmodel.MainViewModel
import com.example.common.CommonActivity
import com.example.common.Preferences
import com.example.mylibrary.MyLibraryActivity
import com.ios.injector.R






class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: MainViewModel

    @DataObserver
    fun didFavoritesDataChanged(data: List<SpeedDialModel>) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.favoritesLiveData

//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}