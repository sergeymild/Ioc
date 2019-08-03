package com.ioc

import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

object ProviderImplementationBuilder {

    fun buildForSingleton(
        provider: DependencyProvider,
        dependencyModel: DependencyModel,
        metadata: InjectMethodMetadata,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()
        builder.add(DependencyTree.get(provider.dependencyModels, metadata, target = target))

        applyIsLoadIfNeed(dependencyModel.dependencies, target)
        val names = provider.dependencyNames(metadata)
        builder.addStatement("\$T \$N = new \$T(\$L)",
            dependencyModel.originalClassName,
            dependencyModel.generatedName,
            provider.module, names)
        return builder.build()
    }
}