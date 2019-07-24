package com.ioc

import com.ioc.ImplementationsSpec.Companion.wrapInLazyIfNeed
import com.ioc.ImplementationsSpec.Companion.wrapInProviderIfNeed
import com.ioc.ImplementationsSpec.Companion.wrapInWakIfNeed
import com.ioc.ProviderImplementationBuilder.buildForSingleton
import com.ioc.common.*
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */
object DependencyTree {

    fun get(dependencyModels: List<DependencyModel>,
            typeUtils: Types,
            target: TargetType? = null): CodeBlock {

        var builder = CodeBlock.builder()
        for (dependency in dependencyModels) {
            if (target?.localScopeDependencies?.containsKey(dependency.originalTypeString) == true) continue
            if (dependency.asTarget) continue
            val packageName = dependency.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (dependency.provideMethod() == null && isAllowedPackage) {
                throw ProcessorException("Can't find implementations of `${dependency.dependency.asType()} ${dependency.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target?.element}`").setElement(target?.element)
            }
            val code = generateCode(dependency, typeUtils, target)

            val wrapInProviderIfNeed = wrapInProviderIfNeed(code.toBuilder(), dependency)
            builder.add(wrapInLazyIfNeed(wrapInProviderIfNeed, dependency).build())
            builder.add(wrapInWakIfNeed(dependency))
        }
        return builder.build()
    }

    private fun generateCode(dependency: DependencyModel, typeUtils: Types, target: TargetType?): CodeBlock {
        val builder = CodeBlock.builder()

        if (dependency.isSingleton) {
            return singleton(dependency)
        }

        // Generate dependency from method provider
        dependency.implementations.filter { it.isMethod }
                .map { ProviderMethodBuilder.build(it, dependency, typeUtils, target) }
                .firstOrNull()
                ?.let { return it }


        // Generate dependency from implementations (i.e. interface implementations)
        dependency.implementations.filter { !it.isMethod }
                .map { buildForSingleton(it, dependency, typeUtils, target) }
                .firstOrNull()
                ?.let { return it }

        // if we here it's mean what we have dependency with arguments constructor or empty constructor
        if (dependency.argumentsConstructor != null) {

            return argumentsConstructor(dependency, typeUtils, target)
                    .add(builder)
                    .build()
        }

        if (dependency.emptyConstructor != null) {
            return CodeBlock.builder()
                    .emptyConstructor(dependency)
                    .add(builder)
                    .build()
        }

        throw ProcessorException("Can't find default constructor or provide method for `${dependency.className}`").setElement(dependency.dependency)
    }
}