package com.ioc

import com.ioc.common.asClassName
import com.ioc.common.emptyCodBlock
import com.ioc.common.message
import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderMethodBuilder {
    fun build(
        provider: ModuleMethodProvider,
        model: DependencyModel,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        DependencyTree.get(model.dependencies, target = target)
            .also { builder.add(it) }

        builder.add(generateWithDependencies(model, provider, target))

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithDependencies(
        model: DependencyModel,
        method: ModuleMethodProvider,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        applyIsLoadIfNeed(model.dependencies, target)
        val names = model.dependencyNames()

        var statementString = "\$T \$N = \$T.\$N(\$L)"
        if (method.isKotlinModule) statementString = "\$T \$N = \$T.INSTANCE.\$N(\$L)"

        builder.addStatement(statementString,
            model.originalClassName,
            model.generatedName,
            method.module.asClassName(),
            method.name,
            names)

        return builder.build()
    }
}