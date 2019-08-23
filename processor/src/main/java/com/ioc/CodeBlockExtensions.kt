package com.ioc

import com.ioc.common.message
import com.ioc.common.singletonClassName
import com.ioc.common.singletonClassPackage
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

val emptyCodBlock = CodeBlock.builder().build()


fun iocGetSingleton(model: DependencyModel): CodeBlock {

    val className = ClassName.get(singletonClassPackage(model), singletonClassName(model))
    return CodeBlock.builder().add("\$T.getInstance()", className).build()
}

fun emptyConstructor(model: DependencyModel): CodeBlock {
    val className = model.originalClassName
    return CodeBlock.builder().add("new \$T()", className).build()
}

fun emptyModuleMethodProvide(model: DependencyModel): CodeBlock {
    val method = model.methodProvider!!
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