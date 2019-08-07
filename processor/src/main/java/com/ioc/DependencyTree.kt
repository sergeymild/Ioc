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
            if (dependency.methodProvider == null && isAllowedPackage) {
                throwsCantFindImplementations(dependency.dependency, target)
            }

            var code = generateCode(dependency, target).toBuilder()
            applyIsLoadIfNeed(dependency.dependencies, target)



            if (!skipCheckLocalScope) {
                code = ProviderGeneration.wrapInProviderClassIfNeed(dependency, code)
                code = LazyGeneration.wrapInLazyClassIfNeed(dependency, code)
                code = WeakGeneration.wrapInWeakIfNeed(dependency, code)
            }
            code = ViewModelGeneration.wrapInAndroidViewModelIfNeed(dependency, code)
            builder.add(code.build())
        }
        return builder.build()
    }

    private fun generateCode(model: DependencyModel, target: TargetType?): CodeBlock {
        if (model.isSingleton) return emptyCodBlock
        if (model.isViewModel) return get(model.dependencies, target = target)

        model.methodProvider?.let {
            return ProviderMethodBuilder.build(it, model, target)
        }

        // if we here it's mean what we have dependency with arguments constructor or empty constructor
        if (model.constructor != null) {
            val dependencies = get(model.dependencies, target = target)
            val builder = CodeBlock.builder().add(dependencies)

            applyIsLoadIfNeed(model.dependencies, target)

            val names = model.dependencyNames()
            return builder.addStatement("\$T \$N = new \$T(\$L)",
                model.originalClassName,
                model.generatedName,
                model.originalClassName,
                names).build()
        }

        throw didNotFindConstructorOrMethodProvider(model.dependency)
    }
}