package com.ioc

import com.ioc.common.iocType
import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

fun iocGetSingleton(model: DependencyModel): CodeBlock {
    val className = model.originalClassName
    return CodeBlock.builder().add("\$T.singleton(\$T.class)", iocType, className).build()
}

fun emptyConstructor(model: DependencyModel): CodeBlock {
    val className = model.originalClassName
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

fun argumentsConstructor(model: DependencyModel, target: TargetType?
): CodeBlock {
    val dependencies = DependencyTree.get(model.dependencies, target = target)
    val builder = CodeBlock.builder().add(dependencies)

    applyIsLoadIfNeed(model.dependencies, target)

    val names = model.dependencyNames()
    return builder.addStatement("\$T \$N = new \$T(\$L)",
        model.originalClassName,
        model.generatedName,
        model.originalClassName,
        names).build()
}
