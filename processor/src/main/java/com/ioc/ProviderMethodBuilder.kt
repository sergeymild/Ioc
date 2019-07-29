package com.ioc

import com.ioc.common.emptyCodBlock
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderMethodBuilder {
    fun build(
        provider: DependencyProvider,
        dependencyModel: DependencyModel,
        typeUtils: Types,
        target: TargetType?): CodeBlock {

        if (provider.dependencyModels.isEmpty()) {
            return generateWithoutDependencies(dependencyModel, provider)
        }

        val builder = CodeBlock.builder()

        if (!provider.isSingleton) {
            DependencyTree.get(provider.dependencyModels, typeUtils, target)
                .also { builder.add(it) }
        }

        builder.add(generateWithDependencies(dependencyModel, provider, target))

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithDependencies(
        dependencyModel: DependencyModel,
        method: DependencyProvider,
        target: TargetType?): CodeBlock {
        if (method.isSingleton) return emptyCodBlock

        val builder = CodeBlock.builder()

        applyIsLoadIfNeed(dependencyModel.dependencies, target)
        val names = method.dependencyNames()

        var statementString = "\$T \$N = \$T.\$N($names)"
        if (method.isKotlinModule) statementString = "\$T \$N = \$T.INSTANCE.\$N($names)"

        builder.addStatement(statementString,
            dependencyModel.className,
            dependencyModel.generatedName,
            method.module,
            method.name)

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithoutDependencies(dependencyModel: DependencyModel, method: DependencyProvider): CodeBlock {
        if (method.isSingleton) return emptyCodBlock

        var code = CodeBlock.builder()

        var statementString = "\$T \$N = \$T.\$N()"
        if (method.isKotlinModule) statementString = "\$T \$N = \$T.INSTANCE.\$N()"
        code = code.addStatement(statementString,
            dependencyModel.originalClassName(),
            dependencyModel.generatedName,
            method.module,
            method.name)

        return code.build()
    }
}