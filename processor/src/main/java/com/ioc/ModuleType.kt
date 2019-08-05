package com.ioc

import com.squareup.javapoet.CodeBlock
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
    val blocks = dependencies.map { dependencyName(it) }
    return CodeBlock.join(blocks, ",")
    //return dependencies.joinToString { dependencyName(it) }
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
    var dependencies: MutableList<DependencyModel> = mutableListOf()

    override fun toString(): String {
        return "DependencyProvider(isSingleton=$isSingleton, module=$module, dependencies=$dependencies, name='$name', named='$named')"
    }
}