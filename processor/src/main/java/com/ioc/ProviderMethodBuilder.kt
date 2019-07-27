package com.ioc

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
        target: TargetType?,
        usedSingletons: Map<String, DependencyModel>): CodeBlock {

        if (provider.dependencyModels.isEmpty()) {
            return generateWithoutDependencies(dependencyModel, provider)
        }

        val builder = CodeBlock.builder()

        if (!provider.isSingleton) {
            DependencyTree.get(provider.dependencyModels, typeUtils, usedSingletons, target)
                .also { builder.add(it) }
        }

        builder.add(generateWithDependencies(dependencyModel, provider, target, usedSingletons))

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithDependencies(
        dependencyModel: DependencyModel,
        method: DependencyProvider,
        target: TargetType?,
        usedSingletons: Map<String, DependencyModel>): CodeBlock {
        val builder = CodeBlock.builder()

        if (!method.isSingleton) {
            applyIsLoadIfNeed(dependencyModel.dependencies, target, usedSingletons)
            val names = method.dependencyNames()

            var statementString = "\$T \$N = \$T.\$N($names)"
            if (method.isKotlinModule) statementString = "\$T \$N = \$T.INSTANCE.\$N($names)"

            builder.addStatement(statementString,
                dependencyModel.className,
                dependencyModel.generatedName,
                method.module,
                method.name)
        } else {
            if (usedSingletons.containsKey(dependencyModel.typeElementString)) {
                return CodeBlock.builder().build()
            }
            return singleton(dependencyModel)
        }

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithoutDependencies(dependencyModel: DependencyModel, method: DependencyProvider): CodeBlock {
        if (method.isSingleton) {
            return singleton(dependencyModel)
        }

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