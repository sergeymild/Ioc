package com.ioc

import com.ioc.common.asClassName
import com.ioc.common.emptyCodBlock
import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderMethodBuilder {
    fun build(
        provider: ModuleMethodProvider,
        dependencyModel: DependencyModel,
        target: TargetType?): CodeBlock {
        if (provider.isSingleton) return emptyCodBlock

        val builder = CodeBlock.builder()

        if (!provider.isSingleton) {
            DependencyTree.get(provider.dependencyModels, target = target)
                .also { builder.add(it) }
        }

        builder.add(generateWithDependencies(dependencyModel, provider, target))

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithDependencies(
        model: DependencyModel,
        method: ModuleMethodProvider,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        applyIsLoadIfNeed(model.dependencies, target)
        val names = method.dependencyNames()

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