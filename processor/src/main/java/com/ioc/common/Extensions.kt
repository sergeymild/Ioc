package com.ioc.common

import com.ioc.IProcessor
import com.squareup.javapoet.*
import kotlinx.metadata.Flag
import java.util.concurrent.TimeUnit
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic


/**
 * Created by sergeygolishnikov on 20/11/2017.
 */


fun message(message: Any?) {
    IProcessor.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "$message")
}


inline fun measure(message: String, block: () -> Unit) {
    val start = System.currentTimeMillis()
    try {
        block()
    } finally {
        val end = System.currentTimeMillis()
        message("$message: [mills: ${end - start}]")
    }
}


fun isModuleKotlinObject(typeElement: TypeElement): Boolean {
    val kmClass = KotlinUtil.kmClassOf(typeElement) ?: return false
    return Flag.Class.IS_OBJECT.invoke(kmClass.flags)
}

fun isKotlinCompanionObject(typeElement: TypeElement): Boolean {
    val kmClass = KotlinUtil.kmClassOf(typeElement) ?: return false
    return Flag.Class.IS_COMPANION_OBJECT.invoke(kmClass.flags)
}

fun Element?.isSupportedType(): Boolean {
    this ?: return false
    if (asType().kind == TypeKind.ARRAY) return false
    if (asType().kind == TypeKind.VOID) return false
    if (asType().kind == TypeKind.NONE) return false
    if (asType().kind == TypeKind.NULL) return false
    if (asType().kind == TypeKind.ERROR) return false
    if (asType().kind == TypeKind.TYPEVAR) return false
    if (asType().kind == TypeKind.WILDCARD) return false
    if (asType().kind == TypeKind.PACKAGE) return false
    if (asType().kind == TypeKind.OTHER) return false
    if (asType().kind == TypeKind.UNION) return false
    if (asType().kind == TypeKind.INTERSECTION) return false
    if (asType().kind.isPrimitive) return false
    return true
}