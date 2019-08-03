package com.ioc

import com.ioc.common.emptyCodBlock
import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderMethodBuilder {
    fun build(
        provider: DependencyProvider,
        dependencyModel: DependencyModel,
        metadata: InjectMethodMetadata,
        target: TargetType?): CodeBlock {
        if (provider.isSingleton) return emptyCodBlock

        val builder = CodeBlock.builder()

        if (!provider.isSingleton) {
            DependencyTree.get(provider.dependencyModels, metadata, target = target)
                .also { builder.add(it) }
        }

        builder.add(generateWithDependencies(dependencyModel, provider, metadata, target))

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithDependencies(
        model: DependencyModel,
        method: DependencyProvider,
        metadata: InjectMethodMetadata,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        applyIsLoadIfNeed(model.dependencies, target)
        val names = method.dependencyNames(metadata)

        var statementString = "\$T \$N = \$T.\$N(\$L)"
        if (method.isKotlinModule) statementString = "\$T \$N = \$T.INSTANCE.\$N(\$L)"

        builder.addStatement(statementString,
            model.originalClassName,
            model.generatedName,
            method.module,
            method.name, names)

        return builder.build()
    }
}