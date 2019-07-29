package com.ioc


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ios.injector.R
import javax.inject.Inject
import javax.inject.Singleton

class CategoriesRepository
class CategoriesProvider
@Singleton
class RoomDataSource

@Dependency
fun provideCategoriesRepository(dataSource: RoomDataSource): CategoriesRepository = CategoriesRepository()

@Singleton
@Dependency
fun provideCategoriesProvider(
    categoriesRepository: CategoriesRepository
): CategoriesProvider = CategoriesProvider()


//@InjectParentDependencies
class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var categoriesProvider: CategoriesProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(Ioc.singleton(RoomDataSource::class.java))

//        try {
//            Class.forName("androidx.test.espresso.Espresso")
//            findViewById<TextView>(R.id.test_text).setText("Test")
//        } catch (e: ClassNotFoundException) {
//        }
//

    }
}