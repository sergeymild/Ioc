package com.ioc

import com.ioc.common.scopeFactoryType
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderMethodBuilder {
    fun build(provider: DependencyProvider, dependencyModel: DependencyModel, typeUtils: Types, target: TargetType?): CodeBlock {
        if (provider.isFromTarget) {
            return CodeBlock.builder().addStatement("\$T \$N = target.\$N()",
                    dependencyModel.originalClassName(),
                    dependencyModel.generatedName,
                    provider.name).build()
        }

        if (provider.dependencyModels.isEmpty()) {
            return generateWithoutDependencies(dependencyModel, provider)
        }

        val builder = CodeBlock.builder()

        if (!provider.isSingleton && provider.scoped == ROOT_SCOPE) {
            DependencyTree.get(provider.dependencyModels, typeUtils, target)
                    .also { builder.add(it) }
        }

        builder.add(generateWithDependencies(dependencyModel, provider))

        // Module method with params
//        if (dependencyModel.isProvider) {
//            return ImplementationsSpec.wrapInProviderIfNeed(builder, dependencyModel).build()
//        }

        return builder.build()
    }

    @Throws(Throwable::class)
    private fun generateWithDependencies(dependencyModel: DependencyModel, method: DependencyProvider): CodeBlock {
        val builder = CodeBlock.builder()

        if (dependencyModel.scoped != ROOT_SCOPE) {
            return builder.addStatement("\$T \$N = \$T.get(target, \$S, \$S)",
                    dependencyModel.originalClassName(),
                    dependencyModel.generatedName,
                    scopeFactoryType,
                    dependencyModel.scoped,
                    dependencyModel.name).build()
        }

        if (!method.isSingleton) {
            val names = method.dependencyNames()
            builder.addStatement("\$T \$N = \$T.\$N($names)",
                    dependencyModel.className,
                    dependencyModel.generatedName,
                    method.module,
                    method.name)
        } else {
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

        if (dependencyModel.scoped != ROOT_SCOPE) {
            return code.addStatement("\$T \$N = \$T.get(target, \$S, \$S)",
                    dependencyModel.originalClassName(),
                    dependencyModel.generatedName,
                    scopeFactoryType,
                    dependencyModel.scoped,
                    dependencyModel.name).build()
        }

        code = code.addStatement("\$T \$N = \$T.\$N()",
                dependencyModel.originalClassName(),
                dependencyModel.generatedName,
                method.module,
                method.name)

        return code.build()
    }
}