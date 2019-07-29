package com.ioc


import com.ioc.ProviderImplementationBuilder.buildForSingleton
import com.ioc.common.*
import com.squareup.javapoet.CodeBlock
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */
object DependencyTree {

    fun generateWithLocalScope(
        dependencyModels: List<DependencyModel>,
        typeUtils: Types,
        target: TargetType? = null): CodeBlock {

        val builder = CodeBlock.builder()
        for (dependency in dependencyModels) {
            if (dependency.asTarget) continue
            val packageName = dependency.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (dependency.provideMethod() == null && isAllowedPackage) {
                throw ProcessorException("Can't find implementations of `${dependency.dependency.asType()} ${dependency.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target?.element}`").setElement(target?.element)
            }
            var code = generateCode(dependency, typeUtils, target).toBuilder()
            applyIsLoadIfNeed(dependency.dependencies, target)


            code = ProviderGeneration.wrapInProviderClassIfNeed(dependency, code)
            code = LazyGeneration.wrapInLazyClassIfNeed(dependency, code)
            code = WeakGeneration.wrapInWeakIfNeed(dependency, code)
            code = ViewModelGeneration.wrapInAndroidViewModelIfNeed(dependency, code)
            builder.add(code.build())
        }
        return builder.build()
    }

    fun get(dependencyModels: List<DependencyModel>, typeUtils: Types, target: TargetType? = null): CodeBlock {

        val builder = CodeBlock.builder()
        for (dependency in dependencyModels) {
            if (target?.localScopeDependencies?.containsKey(dependency.originalTypeString) == true) continue
            if (dependency.asTarget) continue
            val packageName = dependency.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (dependency.provideMethod() == null && isAllowedPackage) {
                throw ProcessorException("Can't find implementations of `${dependency.dependency.asType()} ${dependency.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target?.element}`").setElement(target?.element)
            }
            var code = generateCode(dependency, typeUtils, target).toBuilder()



            code = ProviderGeneration.wrapInProviderClassIfNeed(dependency, code)
            code = LazyGeneration.wrapInLazyClassIfNeed(dependency, code)
            code = WeakGeneration.wrapInWeakIfNeed(dependency, code)
            builder.add(code.build())
        }
        return builder.build()
    }

    private fun generateCode(
        dependency: DependencyModel,
        typeUtils: Types,
        target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        if (dependency.isSingleton) return emptyCodBlock

        if (dependency.isViewModel) {
            return get(dependency.dependencies, typeUtils, target)
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