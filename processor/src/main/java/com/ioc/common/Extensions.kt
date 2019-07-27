package com.ioc.common

import com.ioc.IProcessor
import com.ioc.IocLazy
import com.ioc.IocProvider
import com.ioc.ViewModelFactoryAnonymousClass
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic
import kotlinx.metadata.Flag
import javax.lang.model.element.TypeElement


/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

val iocLazyType = ClassName.get(IocLazy::class.java)
val iocProviderType = ClassName.get(IocProvider::class.java)
val weakType = ClassName.get(WeakReference::class.java)
val viewModelFactoryType = ClassName.bestGuess("android.arch.lifecycle.ViewModelProvider.Factory")
val keepAnnotation = ClassName.bestGuess("android.support.annotation.Keep")
val nonNullAnnotation = ClassName.bestGuess("android.support.annotation.NonNull")
val viewModelProvidersType = ClassName.bestGuess("android.arch.lifecycle.ViewModelProviders")
val viewModelType = ClassName.bestGuess("android.arch.lifecycle.ViewModel")
val emptyCodBlock = CodeBlock.builder().build()

fun Element.asTypeName(): TypeName {
    return ClassName.get(asType())
}

fun CodeBlock.add(codeBlock: CodeBlock): CodeBlock {
    return toBuilder().add(codeBlock).build()
}

fun CodeBlock.add(block: CodeBlock.Builder): CodeBlock.Builder {
    return block.add(this)
}

fun Element.asLazyType(): TypeName {
    return ParameterizedTypeName.get(iocLazyType, asTypeName())
}

fun Element.asProviderType(): TypeName {
    return ParameterizedTypeName.get(iocProviderType, asTypeName())
}

fun Element.asWeakType(): TypeName {
    return ParameterizedTypeName.get(weakType, asTypeName())
}

fun viewModelFactoryCode(name: String, code: CodeBlock.Builder): CodeBlock.Builder {
    return CodeBlock.builder().add("\$T \$N = \$L;\n",
        viewModelFactoryType,
        "factory_$name",
        ViewModelFactoryAnonymousClass.get(code.build()))
}


fun message(message: Any?) {
    IProcessor.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "$message")
}


inline fun measure(message: String, block: () -> Unit) {
    val start = System.currentTimeMillis()
    try {
        block()
    } finally {
        val end = System.currentTimeMillis()
        com.ioc.common.message("$message: [secs: ${TimeUnit.MILLISECONDS.toSeconds(end - start)} original: ${end - start}]")
//        println("$message: [secs: ${TimeUnit.MILLISECONDS.toSeconds(end - start)} original: ${end - start}]")
    }
}


fun isModuleKotlinObject(typeElement: TypeElement): Boolean {
    val kmClass = KotlinUtil.kmClassOf(typeElement) ?: return false
    return Flag.Class.IS_OBJECT.invoke(kmClass.flags)
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