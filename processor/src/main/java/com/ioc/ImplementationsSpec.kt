package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
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
    fun inject(
        singletonsToInject: List<DependencyModel>,
        emptyConstructorToInject: List<DependencyModel>,
        emptyModuleMethodToInject: MutableList<DependencyModel>): TypeSpec {

        val builder = TypeSpec.classBuilder("${target.name}Injector")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(keepAnnotation)

        generateMethods(singletonsToInject, emptyConstructorToInject, emptyModuleMethodToInject).forEach { builder.addMethod(it) }

        methods.forEach { builder.addMethod(it.methodSpec) }

        return builder.build()
    }

    private fun generateMethods(
        singletonsToInject: List<DependencyModel>,
        emptyConstructorToInject: List<DependencyModel>,
        emptyModuleMethodToInject: MutableList<DependencyModel>
    ): List<MethodSpec> {

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

        for (dependency in singletonsToInject) {
            builder.addCode(setInTarget(dependency, iocGetSingleton(dependency)))
        }

        for (dependency in emptyConstructorToInject) {
            builder.addCode(setInTarget(dependency, emptyConstructor(dependency)))
        }

        for (dependency in emptyModuleMethodToInject) {
            builder.addCode(setInTarget(dependency, emptyModuleMethodProvide(dependency)))
        }

        for (method in this.methods) {
            if (method.methodSpec.returnType == TypeName.VOID) {
                builder.addStatement("\$N(target)", method.methodSpec.name)
                continue
            }

            val callMethodCode = if (method.isTargetUsedAsDependency) {
                CodeBlock.of("\$N(target)", method.methodSpec.name)
            } else {
                CodeBlock.of("\$N()", method.methodSpec.name)
            }
            builder.addCode(setInTarget(method.returnTypeDependencyModel!!, callMethodCode))
        }

        val postInitialization = target.postInitialization
        if (postInitialization != null) {
            builder.addStatement("target.\$N()", postInitialization.simpleName)
        }

        methods.add(builder.build())
        return methods
    }

    companion object {

        fun provideInjectionMethod(
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
                    iocType,
                    ClassName.bestGuess(singletonName))
            }

            val methodName = "provide${model.originalType.simpleName.capitalize()}"
            val methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .returns(model.returnType())
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
    }

}