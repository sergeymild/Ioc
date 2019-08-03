package com.ioc

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun dependencyName(model: DependencyModel, metadata: InjectMethodMetadata) = when {
    model.isSingleton -> iocGetSingleton(model)
    model.isLocal -> CodeBlock.of("target.\$N", model.fieldName)
    else -> CodeBlock.of("\$N", model.generatedName)
}

fun DependencyProvider.dependencyNames(metadata: InjectMethodMetadata): CodeBlock {
    val blocks = dependencyModels.map { dependencyName(it, metadata) }
    return CodeBlock.join(blocks, ",")
    //return dependencyModels.joinToString { dependencyName(it) }
}

fun SingletonWrapper.dependencyNames(metadata: InjectMethodMetadata): CodeBlock {
    val blocks = dependencies.map { dependencyName(it, metadata) }
    return CodeBlock.join(blocks, ",")
}

fun DependencyModel.dependencyNames(metadata: InjectMethodMetadata): CodeBlock {
    val blocks = dependencies.map { dependencyName(it, metadata) }
    return CodeBlock.join(blocks, ",")
}

class DependencyProvider(
    var method: Element,
    var isSingleton: Boolean,
    var module: TypeName) {
    var isKotlinModule = false
    var dependencyModels: MutableList<DependencyModel> = mutableListOf()
    var name: CharSequence = method.simpleName
    var named: String? = null
    var isMethod: Boolean = true
    var packageName: String = ""

    override fun toString(): String {
        return "DependencyProvider(method=$method, isSingleton=$isSingleton, module=$module, dependencyModels=$dependencyModels, name='$name', named='$named')"
    }
}