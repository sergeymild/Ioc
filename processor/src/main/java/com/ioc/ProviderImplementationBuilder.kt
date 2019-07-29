package com.ioc

import com.ioc.common.emptyCodBlock
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderImplementationBuilder {

    fun buildForSingleton(
        provider: DependencyProvider,
        dependencyModel: DependencyModel,
        typeUtils: Types,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        if (!provider.isSingleton) {
            DependencyTree.get(provider.dependencyModels, typeUtils, target)
                .also { builder.add(it) }
        }

        if (provider.isSingleton) return emptyCodBlock

        applyIsLoadIfNeed(dependencyModel.dependencies, target)
        val names = provider.dependencyNames()
        builder.addStatement("\$T \$N = new \$T(\$L)",
            dependencyModel.originalClassName(),
            dependencyModel.generatedName,
            provider.module, names)
        return builder.build()
    }
}