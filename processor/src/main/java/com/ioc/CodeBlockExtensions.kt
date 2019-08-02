package com.ioc

import com.ioc.common.asTypeElement
import com.ioc.common.iocType
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */
private val SINGLETON = "Singleton"

fun iocGetSingleton(model: DependencyModel): CodeBlock {
    val singletonName = model.dependency.asTypeElement().qualifiedName.toString()
    val className = ClassName.bestGuess(singletonName)
    return CodeBlock.builder().add("\$T.singleton(\$T.class)", iocType, className).build()
}

fun emptyConstructor(model: DependencyModel): CodeBlock {
    val singletonName = model.dependency.asTypeElement().qualifiedName.toString()
    val className = ClassName.bestGuess(singletonName)
    return CodeBlock.builder().add("new \$T()", className).build()
}

fun emptyModuleMethodProvide(model: DependencyModel): CodeBlock {
    val method = model.provideMethod()!!
    var statementString = "\$T.\$N()"
    if (method.isKotlinModule) statementString = "\$T.INSTANCE.\$N()"
    return CodeBlock.of(statementString, method.module, method.name)
}

fun setInTarget(dependency: DependencyModel, codeBlock: CodeBlock): CodeBlock {
    val setterCodeBlock = CodeBlock.builder()
    if (dependency.setterMethod != null) setterCodeBlock.addStatement("target.\$N(\$L)", dependency.setterName(), codeBlock)
    else setterCodeBlock.addStatement("target.\$N = \$L", dependency.setterName(), codeBlock)
    return setterCodeBlock.build()
}

fun CodeBlock.Builder.emptyConstructor(model: DependencyModel): CodeBlock {
    return addStatement("\$T \$N = new \$T()",
        model.originalType.asType(),
        model.generatedName,
        model.className)
        .build()
}

fun applyIsLoadIfNeed(dependencies: List<DependencyModel>, target: TargetType?) {
    for (dependency in dependencies) {
        val fieldName = target?.localScopeDependencies?.get(dependency.originalTypeString)
        if (fieldName != null) {
            dependency.isLocal = true
            dependency.fieldName = fieldName
        }

        if (target.isSubtype(dependency.originalType)) {
            dependency.generatedName = "target"
        }
    }
}

fun argumentsConstructor(
    model: DependencyModel,
    typeUtils: Types,
    target: TargetType?): CodeBlock {

    val dependencies = DependencyTree.get(model.dependencies, typeUtils, target)
    val builder = CodeBlock.builder().add(dependencies)

    applyIsLoadIfNeed(model.dependencies, target)

    val names = model.dependencyNames()

    return builder.addStatement("\$T \$N = new \$T(\$L)",
        model.originalType.asType(),
        model.generatedName,
        model.className, names).build()
}

fun singletonProvider(model: DependencyModel): String {
    val singletonName = model.dependency.asTypeElement().qualifiedName.toString()
    return CodeBlock.builder()
        .add("\$T.singleton(\$T.class)", ClassName.get(Ioc::class.java), ClassName.bestGuess(singletonName))
        .build().toString()
    //return "com.ioc.Ioc.singleton($singletonName.class)"
}

fun singletonProviderCode(model: DependencyModel): CodeBlock {
    val singletonName = model.dependency.asTypeElement().qualifiedName.toString()
    return CodeBlock.builder()
        .add("\$T.singleton(\$T.class)", ClassName.get(Ioc::class.java), ClassName.bestGuess(singletonName))
        .build()
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