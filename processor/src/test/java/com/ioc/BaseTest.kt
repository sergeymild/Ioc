package com.ioc

/**
 * Created by sergeygolishnikov on 09/08/2017.
 */

val keepAnnotation = "import android.support.annotation.Keep"
val nonNullAnnotation = "import android.support.annotation.NonNull"

interface BaseTest {
    fun Class<*>.import() : String {
        return "import $name;"
    }
}