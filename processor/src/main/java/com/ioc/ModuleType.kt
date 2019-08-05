package com.ioc

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun dependencyName(model: DependencyModel) = when {
    model.isSingleton -> iocGetSingleton(model)
    model.isLocal -> CodeBlock.of("target.\$N", model.fieldName)
    else -> CodeBlock.of("\$N", model.generatedName)
}

fun ModuleMethodProvider.dependencyNames(): CodeBlock {
    val blocks = dependencyModels.map { dependencyName(it) }
    return CodeBlock.join(blocks, ",")
    //return dependencyModels.joinToString { dependencyName(it) }
}

fun DependencyModel.dependencyNames(): CodeBlock {
    val blocks = dependencies.map { dependencyName(it) }
    return CodeBlock.join(blocks, ",")
}

class ModuleMethodProvider(
    var name: CharSequence,
    var module: Element,
    var named: String? = null,
    var isSingleton: Boolean = false,
    var isKotlinModule: Boolean = false) {
    var dependencyModels: MutableList<DependencyModel> = mutableListOf()
    var packageName: String = ""

    override fun toString(): String {
        return "DependencyProvider(isSingleton=$isSingleton, module=$module, dependencyModels=$dependencyModels, name='$name', named='$named')"
    }
}