package com.ioc

import com.ioc.common.iocType
import com.ioc.common.message
import com.squareup.javapoet.CodeBlock
import javax.lang.model.element.Element

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

val emptyCodBlock = CodeBlock.builder().build()


fun iocGetSingleton(model: DependencyModel): CodeBlock {
    // if we have a named annotation prefer get implementation instead of interface
    val type: Element = /*if (model.named == null || model.named == "") model.dependency else */model.originalType
    return CodeBlock.builder().add("\$T.getSingleton(\$T.class)", iocType, type).build()
}

fun emptyConstructor(model: DependencyModel): CodeBlock {
    val className = model.originalClassName
    return CodeBlock.builder().add("new \$T()", className).build()
}

fun emptyModuleMethodProvide(model: DependencyModel): CodeBlock {
    val method = model.methodProvider!!
    var statementString = "\$T.\$N()"


    if (method.isKotlinModule && method.name == "INSTANCE") {
        return CodeBlock.of("\$T.INSTANCE", method.module)
    } else if (method.isKotlinModule) {
        return CodeBlock.of("\$T.INSTANCE.\$N()", method.module, method.name)
    }
    return CodeBlock.of(statementString, method.module, method.name)
}

fun setInTarget(dependency: DependencyModel, codeBlock: CodeBlock): CodeBlock {
    val setterCodeBlock = CodeBlock.builder()
    if (dependency.setterMethod != null) setterCodeBlock.addStatement("target.\$N(\$L)", dependency.setterName(), codeBlock)
    else setterCodeBlock.addStatement("target.\$N = \$L", dependency.setterName(), codeBlock)
    return setterCodeBlock.build()
}

fun applyIsLoadIfNeed(dependencies: List<DependencyModel>, target: TargetType?) {
    for (dependency in dependencies) {
        if (target.isLocalScope(dependency.dependency, dependency.originalType)) {
            dependency.isLocal = true
            dependency.fieldName = target.localScopeName(dependency.dependency, dependency.originalType)
        }

        if (target.isSubtype(dependency.dependency, dependency.originalType)) {
            dependency.generatedName = "target"
        }
    }
}