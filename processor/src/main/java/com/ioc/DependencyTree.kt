package com.ioc


import com.ioc.common.*
import com.squareup.javapoet.CodeBlock

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */
object DependencyTree {

    fun get(
        dependencyModels: List<DependencyModel>,
        skipCheckLocalScope: Boolean = false,
        target: TargetType? = null): CodeBlock {

        val builder = CodeBlock.builder()
        for (dependency in dependencyModels) {
            if (!skipCheckLocalScope && target.isLocalScope(dependency.dependency, dependency.originalType)) continue
            if (target.isSubtype(dependency.dependency, dependency.originalType)) continue
            val packageName = dependency.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (dependency.provideMethod() == null && isAllowedPackage) {
                throw ProcessorException("Can't find methodProvider of `${dependency.dependency.asType()} ${dependency.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target?.element}`").setElement(target?.element)
            }

            var code = generateCode(dependency, target).toBuilder()
            applyIsLoadIfNeed(dependency.dependencies, target)



            code = ProviderGeneration.wrapInProviderClassIfNeed(dependency, code)
            code = LazyGeneration.wrapInLazyClassIfNeed(dependency, code)
            code = WeakGeneration.wrapInWeakIfNeed(dependency, code)
            code = ViewModelGeneration.wrapInAndroidViewModelIfNeed(dependency, code)
            builder.add(code.build())
        }
        return builder.build()
    }

    private fun generateCode(dependency: DependencyModel, target: TargetType?): CodeBlock {

        val builder = CodeBlock.builder()

        if (dependency.isSingleton) return emptyCodBlock

        if (dependency.isViewModel) return get(dependency.dependencies, target = target)

        dependency.methodProvider?.let {
            return ProviderMethodBuilder.build(it, dependency, target)
        }

        // if we here it's mean what we have dependency with arguments constructor or empty constructor
        if (dependency.argumentsConstructor != null) {
            return argumentsConstructor(dependency, target).add(builder).build()
        }

        if (dependency.emptyConstructor != null) {
            return CodeBlock.builder().emptyConstructor(dependency).add(builder).build()
        }

        throw ProcessorException("Can't find default constructor or provide method for `${dependency.dependencyTypeString}`").setElement(dependency.dependency)
    }
}