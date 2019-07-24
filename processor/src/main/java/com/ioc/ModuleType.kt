package com.ioc

import com.ioc.common.asElement
import com.ioc.common.asTypeElement
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun dependencyName(model: DependencyModel) = when {
    model.asTarget -> "target"
    model.isLocal -> "target.${model.fieldOrGetterName()}"
    else -> model.generatedName
}

fun DependencyProvider.dependencyNames(): String {
    return dependencyModels.joinToString { dependencyName(it) }
}

fun SingletonWrapper.dependencyNames(): String {
    return dependencies.joinToString { dependencyName(it) }
}

fun DependencyModel.dependencyNames(): String {
    return dependencies.joinToString { dependencyName(it) }
}

class DependencyProvider constructor(
    var method: Element,
    var isSingleton: Boolean,
    var module: TypeName) {
    var methodType: ExecutableElement? = null
    var returnTypes = mutableListOf<TypeMirror>()
    var dependencyModels: MutableList<DependencyModel> = mutableListOf()
    var name = method.simpleName.toString()
    var named: String? = null
    var isMethod: Boolean = true
    var isLocal: Boolean = false
    var isFromTarget: Boolean = false
    var packageName: String = ""

    fun returnType(): TypeElement {
        return returnTypes[0].asElement().asTypeElement()
    }

    override fun toString(): String {
        return "DependencyProvider(method=$method, isSingleton=$isSingleton, module=$module, returnTypes=$returnTypes, dependencyModels=$dependencyModels, name='$name', named='$named')"
    }
}