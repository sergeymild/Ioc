package com.ioc

import com.ioc.common.iocLazyType
import com.ioc.common.iocProviderType
import com.ioc.common.isHasAnnotation
import com.ioc.common.weakType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

fun Element.asTypeName(): TypeName {
    return ClassName.get(asType())
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

fun postInitializationMethod(element: TypeElement): ExecutableElement? {
    val postInitializationMethod = element.methods { it.isHasAnnotation(PostInitialization::class.java) }.firstOrNull() ?: return null
    validatePostInitializationMethod(postInitializationMethod)
    return postInitializationMethod
}

fun Element.asTypeString(): String = asType().toString()

fun Element.methods(predicate: (ExecutableElement) -> Boolean): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements).filter(predicate)
}

fun Element.methods(): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements)
}

fun Element.dependencyMethods(): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements)
        .filter { it.isHasAnnotation(Dependency::class.java) }
}

