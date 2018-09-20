package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.*
import javax.inject.Provider
import javax.lang.model.element.Modifier
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

internal fun targetParameter(className: ClassName): ParameterSpec {
    return ParameterSpec.builder(className, "target", Modifier.FINAL)
            .addAnnotation(nonNullAnnotation)
            .build()
}

class ImplementationsSpec constructor(private val target: TargetType,
                                      private val typeUtils: Types,
                                      private val methods: List<MethodSpec>,
                                      private val dependencies: Set<DependencyModel>) {

    init {
        target.parentsDependencies()
    }

    @Throws(Throwable::class)
    fun inject(): TypeSpec {

        val builder = TypeSpec.classBuilder("${target.name}Injector")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)

        generateMethods().forEach { builder.addMethod(it) }

        methods.forEach { builder.addMethod(it) }

        return builder.build()
    }

    private fun generateMethods(): List<MethodSpec> {
        val methods = mutableListOf<MethodSpec>()

        val builder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)
                .addParameter(targetParameter(target.className))

        // Call super for parent inject
        target.firstParentWithDependencies()?.let {
            val parentType = it.className
            val injectorType = ClassName.get(parentType.packageName(), "${parentType.simpleName()}Injector")
            builder.addStatement("new \$T().inject(target)", injectorType)
        }

        // generate methods for scoped classesWithDependencyAnnotation
        //val cachedMethods = generateScopeCachedMethods()

        //cachedMethods.forEach { builder.addStatement("\$N(target)", it.name) }

        this.methods.forEach {
            builder.addStatement("\$N(target)", it.name)
        }

        val postInitialization = target.postInitialization
        if (postInitialization != null) {
            builder.addStatement("target.\$N()", postInitialization.simpleName)
        }

        methods.add(builder.build())

        //methods.addAll(cachedMethods)


        return methods
    }

    companion object {
        fun cachedMethod(target: TargetType, typeUtils: Types, dependency: DependencyModel): MethodSpec {
            val originalType = dependency.originalType
            dependency.originalType = dependency.dependency
            try {
                return MethodSpec.methodBuilder(dependency.cacheMethodName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .addParameter(targetParameter(target.className))
                        .addCode(dependencyInjectionCode(dependency, typeUtils, target)
                                .addStatement("\$T.cache(target, \$S, \$S, \$N)",
                                        scopeFactoryType,
                                        target.rootScope,
                                        dependency.name,
                                        dependency.generatedName)
                                .build())
                        .build()
            } finally {
                dependency.originalType = originalType
            }
        }

        fun dependencyInjectionMethod(target: ClassName,
                                      dependencyModel: DependencyModel,
                                      codeBlock: CodeBlock,
                                      methodName: String = dependencyModel.injectMethodName): MethodSpec.Builder {

            return MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .addParameter(targetParameter(target))
                    .addCode(codeBlock)
        }

        fun wrapInLazyIfNeed(codeBlock: CodeBlock.Builder,
                             dependencyModel: DependencyModel): CodeBlock.Builder {

            if (!dependencyModel.isLazy) return codeBlock
            val providerGeneric = dependencyModel.erasuredType.asTypeElement()
            val originalGeneratedName = dependencyModel.generatedName
            dependencyModel.generatedName = "lazy_${dependencyModel.generatedName}"
            return lazyCodeBlock(providerGeneric, originalGeneratedName, codeBlock)
        }

        fun wrapInProviderIfNeed(codeBlock: CodeBlock.Builder,
                                 dependencyModel: DependencyModel): CodeBlock.Builder {

            if (!dependencyModel.isProvider) return codeBlock

            val originalGeneratedName = dependencyModel.generatedName
            dependencyModel.generatedName = "provider_$originalGeneratedName"
            val providerGeneric = ClassName.get(dependencyModel.erasuredType)
            val providerType = ParameterizedTypeName.get(ClassName.get(Provider::class.java), providerGeneric)
            val code = codeBlock.addStatement("return \$N", originalGeneratedName).build()
            return CodeBlock.builder().add("\$T \$N = \$L;\n",
                    providerType,
                    dependencyModel.generatedName,
                    ProviderAnonymousClass.get(code, providerGeneric))
        }

        fun wrapInWakIfNeed(dependencyModel: DependencyModel): CodeBlock {
            if (!dependencyModel.isWeakDependency) return emptyCodBlock
            val providerGeneric = dependencyModel.erasuredType.asTypeElement()
            val originalGeneratedName = dependencyModel.generatedName
            dependencyModel.generatedName = "weak_$originalGeneratedName"
            return weakCodeBlock(providerGeneric, originalGeneratedName)
        }

        // Root scope
        //  scope
        fun dependencyInjectionCode(dependency: DependencyModel,
                                    typeUtils: Types,
                                    target: TargetType): CodeBlock.Builder {

            val packageName = dependency.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (dependency.provideMethod() == null && isAllowedPackage) {
                throw ProcessorException("Can't find implementations of `${dependency.dependency.asType()} ${dependency.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target.element}`").setElement(target.element)
            }

            try {
                val builder = CodeBlock.builder()

//                if (dependency.scoped != ROOT_SCOPE) {
//                    val builder = builder.addStatement("\$T \$N = \$T.<\$T>get(\$S, \$T.class)",
//                            dependency.dependency,
//                            dependency.generatedName,
//                            scopeFactoryType,
//                            dependency.dependency,
//                            dependency.scoped,
//                            dependency.dependency)
//                    val wrapInProviderIfNeed = wrapInProviderIfNeed(builder, dependency)
//                    return wrapInLazyIfNeed(wrapInProviderIfNeed, dependency)
//                }

                // TODO scoped?
                if (dependency.scoped != ROOT_SCOPE) {
                    builder.addStatement("\$T \$N = \$T.get(target, \$S, \$S)",
                            dependency.originalClassName(),
                            dependency.generatedName,
                            scopeFactoryType,
                            dependency.scoped,
                            dependency.name)
                    val wrapInProviderIfNeed = wrapInProviderIfNeed(builder, dependency)
                    return wrapInLazyIfNeed(wrapInProviderIfNeed, dependency)
                }

                if (dependency.isViewModel) {
                    val code = CodeBlock.builder()
                    DependencyTree.get(dependency.depencencies, typeUtils, target).also { code.add(it) }
                    val names = dependency.dependencyNames()
                    code.addStatement("return (T) new \$T($names)", dependency.originalClassName())
                    val originalGeneratedName = dependency.generatedName
                    val factoryName = "factory_${dependency.generatedName}"
                    //dependency.generatedName = "viewModel_${dependency.generatedName}"
                    val viewModelBuilder = viewModelFactoryCode(originalGeneratedName, code)

                    viewModelBuilder.addStatement("\$T \$N = \$T.of(target, \$N).get(\$T.class)",
                            dependency.originalClassName(),
                            dependency.generatedName,
                            viewModelProvidersType,
                            factoryName,
                            dependency.originalType)
                    return viewModelBuilder
                }

                dependency.implementations.firstOrNull { it.isMethod }?.let {
                    if (it.isSingleton) {
                        return singleton(dependency).toBuilder()
                    }


                    DependencyTree.get(dependency.depencencies, typeUtils, target).also { builder.add(it) }
                    val names = dependency.dependencyNames()
                    return builder.addStatement("\$T \$N = \$T.\$N(\$L)",
                            dependency.originalClassName(),
                            dependency.generatedName,
                            it.module,
                            it.name,
                            names)
                }

                dependency.implementations.firstOrNull { !it.isMethod }?.let {
                    if (it.isSingleton) {
                        return singleton(dependency).toBuilder()
                    }

                    DependencyTree.get(it.dependencyModels, typeUtils, target)
                            .also { builder.add(it) }
                    val names = it.dependencyNames()
                    return builder.addStatement("\$T \$N = new \$T(\$L)", dependency.originalClassName(), dependency.generatedName, it.returnType(), names)
                }

                if (dependency.isSingleton)
                    return singleton(dependency).toBuilder()

                // Inject with arguments constructor
                if (dependency.argumentsConstructor != null) {
                    return argumentsConstructor(dependency, typeUtils, target)
                            .add(builder)
                }

                // Inject with no arguments constructor
                if (dependency.emptyConstructor != null) {
                    return CodeBlock.builder()
                            .emptyConstructor(dependency)
                            .add(builder)
                }

                return builder
            } finally {
                resetUniqueSingletons()
            }
        }

        fun injectInTarget(builder: MethodSpec.Builder, dependency: DependencyModel): MethodSpec {
            builder.addCode(dependency.setDependency("target.${dependency.setterName()}", dependency.generatedName))

            if (dependency.isFromTarget && dependency.scoped != ROOT_SCOPE) {
                builder.addStatement("\$T.cache(target, \$S, \$S, \$N)",
                        scopeFactoryType,
                        dependency.scoped,
                        dependency.name,
                        dependency.generatedName)
            }

            return builder.build()
        }
    }

}