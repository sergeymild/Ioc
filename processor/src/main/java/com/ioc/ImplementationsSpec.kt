package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

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

    @Throws(Throwable::class)
    fun inject(
        singletonsToInject: List<DependencyModel>,
        emptyConstructorToInject: List<DependencyModel>,
        emptyModuleMethodToInject: MutableList<DependencyModel>): TypeSpec {

        val builder = TypeSpec.classBuilder(targetInjectionClassName(target))
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
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addAnnotation(keepAnnotation)
            .addParameter(targetParameter(target.className))

        // Call super for parent inject
        target.firstParentWithDependencies()?.let {
            val parentType = it.className
            val injectorType = ClassName.get(parentType.packageName(), "${parentType.simpleName()}Injector")
            builder.addStatement("\$T.inject(target)", injectorType)
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

            val methodName = "provide${model.originalType.simpleName.capitalize()}"
            val methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .returns(model.returnType())
                .addCode(body.build())

            if (isTargetUsedAsDependency) methodBuilder.addParameter(targetParameter(target))
            methodBuilder.addStatement("return \$N", model.generatedName)
            return methodBuilder
        }

        fun dependencyInjectionCode(target: TargetType, model: DependencyModel): CodeBlock.Builder {

            val packageName = model.originalType.asTypeElement().getPackage()
            val isAllowedPackage = excludedPackages.any { packageName.toString().startsWith(it) }
            if (model.methodProvider == null && isAllowedPackage) {
                throwCantFindImplementations(model.dependency, target)
            }

            val builder = CodeBlock.builder()

            val code = DependencyTree.get(listOf(model), skipCheckLocalScope = true, target = target)
            builder.add(code)
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
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
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