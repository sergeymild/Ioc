package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.common.CommonActivity
import com.example.common.Preferences
import com.example.mylibrary.MyLibraryActivity
import com.ios.injector.R
import javax.inject.Inject

object Module {
    @Dependency
    fun provideExampleDependencyString(): String {
        return "example"
    }

    @Dependency
    fun prefs(): Preferences? = null
}

interface IParent {
    fun s(): String
}

@Dependency
class Parent {


}

open class ParentActivity: CommonActivity() {
    @Inject
    lateinit var parent: Parent
}

open class AnotherParentActivity: ParentActivity() {
    @Inject
    lateinit var string: String
}

open class OneMoreParentActivity: ParentActivity() {
    @Inject
    lateinit var string: String
}

open class TwoMoreParentActivity: OneMoreParentActivity() {
    @Inject
    lateinit var string2: String
}

open class FourMoreParentActivity: ThreeMoreParentActivity() {
    @Inject
    lateinit var string3: String
}

open class ThreeMoreParentActivity: OneMoreParentActivity() {
    @Inject
    lateinit var string2: String
}


class MainActivity : ParentActivity() {

    @Inject
    lateinit var myLibraryActivity: MyLibraryActivity

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