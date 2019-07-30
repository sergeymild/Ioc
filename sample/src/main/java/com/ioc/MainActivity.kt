package com.ioc


import android.content.Context
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject


class WebViewCoordinatorView
@Inject
constructor(context: Context)

class MyView(context: Context) : FrameLayout(context) {

    @Inject
    lateinit var webViewCoordinatorView: WebViewCoordinatorView

    @LocalScope
    val localContext: Context = context
}

//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


//    @Inject
//    lateinit var myViewModel: MyViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//
//
////        try {
////            Class.forName("androidx.test.espresso.Espresso")
////            findViewById<TextView>(R.id.test_text).setText("Test")
////        } catch (e: ClassNotFoundException) {
////        }
////
//
//    }
//
//    @DataObserver
//    fun dataObserver(string: String) {
//
//    }
}