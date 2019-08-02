package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

class InjectMethod(
    val methodSpec: MethodSpec,
    val isTargetUsedAsDependency: Boolean,
    val returnTypeDependencyModel: DependencyModel?)

internal fun targetParameter(className: ClassName): ParameterSpec {
    return ParameterSpec.builder(className, "target", Modifier.FINAL)
        .addAnnotation(nonNullAnnotation)
        .build()
}

class ImplementationsSpec constructor(
    private val target: TargetType,
    private val methods: List<InjectMethod>) {

    init {
        target.parentsDependencies()
    }

    @Throws(Throwable::class)
    fun inject(singletonsToInject: List<DependencyModel>): TypeSpec {

        val builder = TypeSpec.classBuilder("${target.name}Injector")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(keepAnnotation)

        generateMethods(singletonsToInject).forEach { builder.addMethod(it) }

        methods.forEach { builder.addMethod(it.methodSpec) }

        return builder.build()
    }

    private fun generateMethods(singletonsToInject: List<DependencyModel>): List<MethodSpec> {
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

        for (singleton in singletonsToInject) {
            val singletonName = singleton.dependency.asTypeElement().qualifiedName.toString()
            val className = ClassName.bestGuess(singletonName)
            if (singleton.setterMethod != null) {
                builder.addStatement("target.${singleton.setterMethod?.simpleName}(\$T.singleton(\$T.class))", ClassName.get(Ioc::class.java), className)
            } else {
                builder.addStatement("target.${singleton.setterName()} = \$T.singleton(\$T.class)", ClassName.get(Ioc::class.java), className)
            }
        }

        for (method in this.methods) {
            if (method.methodSpec.returnType == TypeName.VOID) {
                if (method.isTargetUsedAsDependency) builder.addStatement("\$N(target)", method.methodSpec.name)
                else builder.addStatement("\$N(target)", method.methodSpec.name)
                continue
            }
            if (method.isTargetUsedAsDependency) {
                if (method.returnTypeDependencyModel?.setterMethod != null) {
                    builder.addStatement("target.${method.returnTypeDependencyModel.setterMethod?.simpleName}(\$N(target))", method.methodSpec.name)
                } else {
                    builder.addStatement("target.${method.returnTypeDependencyModel?.setterName()} = \$N(target)", method.methodSpec.name)
                }
            } else {
                if (method.returnTypeDependencyModel?.setterMethod != null) {
                    builder.addStatement("target.${method.returnTypeDependencyModel.setterMethod?.simpleName}(\$N())", method.methodSpec.name)
                } else {
                    builder.addStatement("target.${method.returnTypeDependencyModel?.setterName()} = \$N()", method.methodSpec.name)
                }
            }
        }

        val postInitialization = target.postInitialization
        if (postInitialization != null) {
            builder.addStatement("target.\$N()", postInitialization.simpleName)
        }

        methods.add(builder.build())
        return methods
    }

    companion object {

        private fun returnType(model: DependencyModel): TypeName {
            return when {
                model.isLazy -> model.originalType.asLazyType()
                model.isProvider -> model.originalType.asProviderType()
                model.isWeakDependency -> model.originalType.asWeakType()
                else -> model.originalClassName()
            }
        }

        fun dependencyInjectionMethod(
            target: ClassName,
            isTargetUsedAsDependency: Boolean,
            model: DependencyModel,
            codeBlock: CodeBlock): MethodSpec.Builder {

            val body = codeBlock.toBuilder()
            if (model.isSingleton) {
                val singletonName = model.dependency.asTypeElement().qualifiedName.toString()
                body.add("\$T \$N = \$T.singleton(\$T.class);\n",
                    model.className,
                    model.generatedName,
                    ClassName.get(Ioc::class.java),
                    ClassName.bestGuess(singletonName))
            }

            val methodName = "inject${model.name.capitalize()}In${model.fieldName.capitalize()}"
            val methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .returns(returnType(model))
                .addCode(body.build())

            if (isTargetUsedAsDependency) methodBuilder.addParameter(targetParameter(target))
            methodBuilder.addStatement("return \$N", model.generatedName)
            return methodBuilder
        }

        fun dependencyInjectionCode(
            dependency: DependencyModel,
            typeUtils: Types,
            target: TargetType): CodeBlock.Builder {

            val packageName = dependency.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (dependency.provideMethod() == null && isAllowedPackage) {
                throw ProcessorException("Can't find implementations of `${dependency.dependency.asType()} ${dependency.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target.element}`").setElement(target.element)
            }

            val builder = CodeBlock.builder()

            val code = DependencyTree.generateWithLocalScope(listOf(dependency), typeUtils, target)
            builder.add(code)
            //applyIsLoadIfNeed(listOf(dependency), target, usedSingletons)
            return builder
        }

        fun addDataObservers(target: TargetType): List<InjectMethod> {
            val methods = mutableListOf<InjectMethod>()
            for (dataObserver in target.dataObservers) {

                val liveDataTypeName = dataObserver.observingType.simpleName.toString()
                val viewModelName = dataObserver.viewModel.simpleName.toString()

                val observerType = ParameterizedTypeName.get(androidLiveDataObserver, dataObserver.observingType.asTypeName())
                val observerClassSpec = TypeSpec.anonymousClassBuilder("")
                    .superclass(observerType)
                    .addMethod(MethodSpec.methodBuilder("onChanged")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(dataObserver.observingType.asTypeName(), "observingData")
                        .addStatement("target.\$N(\$N)", dataObserver.observerMethod.simpleName, "observingData")
                        .build())
                    .build()


                var observeTypeString = "target.\$N.\$N.observe(target, \$L)"
                if (dataObserver.observeType == DataObserver.ObserveType.FOREVER) {
                    observeTypeString = "target.\$N.\$N.observeForever(\$L)"
                }
                methods.add(InjectMethod(MethodSpec
                    .methodBuilder("observe${dataObserver.liveDataName()}${liveDataTypeName}From$viewModelName")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .addParameter(targetParameter(target.className))
                    .addStatement(observeTypeString,
                        dataObserver.targetViewModelField.toString(),
                        dataObserver.viewModelLiveDataField.toString(),
                        observerClassSpec)
                    .build(), false, null))
            }

            return methods
        }

        fun injectInTarget(builder: MethodSpec.Builder, dependency: DependencyModel): MethodSpec {
            builder.addCode(dependency.setDependency("target.${dependency.setterName()}", dependency.generatedName))
            return builder.build()
        }
    }

}