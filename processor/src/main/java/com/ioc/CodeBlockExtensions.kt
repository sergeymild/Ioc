package com.ioc

import com.ioc.common.asTypeElement
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */
private val SINGLETON = "Singleton"

fun CodeBlock.Builder.emptyConstructor(model: DependencyModel, isFromScope: Boolean = false): CodeBlock {
    if (!isFromScope) {
        return addStatement("\$T \$N = new \$T()",
            model.originalType.asType(),
            model.generatedName,
            model.className)
            .build()
    }

    return addStatement("\$N = new \$T()",
        model.generatedName,
        model.className)
        .build()
}

fun applyIsLoadIfNeed(model: DependencyModel, target: TargetType?) {
    for (dependency in model.dependencies) {
        val fieldName = target?.localScopeDependencies?.get(dependency.originalTypeString)
            ?: continue
        dependency.isLocal = true
        dependency.fieldName = fieldName
    }
}

fun argumentsConstructor(
    model: DependencyModel,
    typeUtils: Types,
    target: TargetType?,
    usedSingletons: Map<String, DependencyModel>,
    isFromScope: Boolean = false): CodeBlock {

    val dependencies = DependencyTree.get(model.dependencies, typeUtils, usedSingletons, target)
    val builder = CodeBlock.builder().add(dependencies)

    applyIsLoadIfNeed(model, target)

    val names = model.dependencyNames(usedSingletons)

    if (!isFromScope) {
        return builder.addStatement("\$T \$N = new \$T($names)",
            model.originalType.asType(),
            model.generatedName,
            model.className).build()
    }

    return builder.addStatement("\$N = new \$T($names)",
        model.generatedName,
        model.className).build()
}

fun singleton(model: DependencyModel): CodeBlock {
    val name = model.dependency.simpleName.toString()
    model.generatedName = uniqueName(name).decapitalize()
    val singleton = ClassName.bestGuess("${model.packageName}.${model.dependency.asTypeElement().simpleName}$SINGLETON")
    val type = model.dependency.asTypeElement()
    return CodeBlock.builder()
        .addStatement("\$T \$N = \$T.get()",
            type,
            model.generatedName,
            singleton)
        .build()
}

fun singleton(name: String, model: DependencyModel): CodeBlock {
    val singleton = ClassName.bestGuess("${model.packageName}.${model.dependency.asTypeElement().simpleName}$SINGLETON")
    val type = model.dependency.asTypeElement()
    return CodeBlock.builder().addStatement("\$T \$N = \$T.get()", type, name, singleton).build()
}