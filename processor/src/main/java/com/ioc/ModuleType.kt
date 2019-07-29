package com.ioc

import com.ioc.common.asTypeElement
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun dependencyName(model: DependencyModel) = when {
    model.isSingleton -> singletonProviderCode(model)
    model.asTarget -> CodeBlock.of("target")
    model.isLocal -> CodeBlock.of("target.\$N", model.fieldName)
    else -> CodeBlock.of("\$N", model.generatedName)
}

fun DependencyProvider.dependencyNames(): CodeBlock {
    val blocks = dependencyModels.map { dependencyName(it) }
    return CodeBlock.join(blocks, ",")
    //return dependencyModels.joinToString { dependencyName(it) }
}

fun SingletonWrapper.dependencyNames(): CodeBlock {
    val blocks = dependencies.map { dependencyName(it) }
    return CodeBlock.join(blocks, ",")
}

fun DependencyModel.dependencyNames(): CodeBlock {
    val blocks = dependencies.map { dependencyName(it) }
    return CodeBlock.join(blocks, ",")
}

class DependencyProvider(
    var method: Element,
    var isSingleton: Boolean,
    var module: TypeName) {
    var isKotlinModule = false
    var returnTypes = mutableListOf<TypeMirror>()
    var dependencyModels: MutableList<DependencyModel> = mutableListOf()
    var name = method.simpleName.toString()
    var named: String? = null
    var isMethod: Boolean = true
    var packageName: String = ""

    fun returnType(): TypeElement {
        return returnTypes[0].asTypeElement()
    }

    override fun toString(): String {
        return "DependencyProvider(method=$method, isSingleton=$isSingleton, module=$module, returnTypes=$returnTypes, dependencyModels=$dependencyModels, name='$name', named='$named')"
    }
}