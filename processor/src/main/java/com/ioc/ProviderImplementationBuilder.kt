package com.ioc

import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderImplementationBuilder {

    fun buildForSingleton(
        provider: DependencyProvider,
        dependencyModel: DependencyModel,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()
        builder.add(DependencyTree.get(provider.dependencyModels, target = target))

        applyIsLoadIfNeed(dependencyModel.dependencies, target)
        val names = provider.dependencyNames()
        builder.addStatement("\$T \$N = new \$T(\$L)",
            dependencyModel.originalClassName(),
            dependencyModel.generatedName,
            provider.module, names)
        return builder.build()
    }
}