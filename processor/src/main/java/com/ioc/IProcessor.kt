package com.ioc

import com.ioc.ImplementationsSpec.Companion.dependencyInjectionCode
import com.ioc.ImplementationsSpec.Companion.dependencyInjectionMethod
import com.ioc.ImplementationsSpec.Companion.injectInTarget
import com.ioc.ImplementationsSpec.Companion.wrapInLazyIfNeed
import com.ioc.ImplementationsSpec.Companion.wrapInProviderIfNeed
import com.ioc.ImplementationsSpec.Companion.wrapInWakIfNeed
import com.ioc.common.*
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.properties.Delegates

interface ErrorThrowable {
    @Throws(Throwable::class)
    fun throwError(element: Element? = null, message: String?)
}

@SupportedAnnotationTypes("javax.inject.*")
open class IProcessor : AbstractProcessor(), ErrorThrowable {
    var filer: Filer by Delegates.notNull()
    var createdSingletones = mutableSetOf<String>()
    lateinit var dependencyResolver: DependencyResolver

    companion object {
        val classesWithDependencyAnnotation = mutableListOf<Element>()
        val methodsWithDependencyAnnotation = mutableListOf<ExecutableElement>()
        var singletons = mutableMapOf<String, MutableList<DependencyModel>>()
        var messager: Messager by Delegates.notNull()
        var dependencyFinder: DependencyTypesFinder by Delegates.notNull()
        val qualifierFinder = QualifierFinder()
        lateinit var types: Types

        fun postInitializationMethod(element: TypeElement): ExecutableElement? {
            val postInitializationMethod = element.methods { it.isHasAnnotation(PostInitialization::class.java) }.firstOrNull()
            if (postInitializationMethod != null && postInitializationMethod.isPrivate()) {
                throw ProcessorException("@PostInitialization placed on `${postInitializationMethod.simpleName}` in ${postInitializationMethod.enclosingElement} with private access").setElement(postInitializationMethod)
            }

            if (postInitializationMethod != null && postInitializationMethod.parameters.isNotEmpty()) {
                throw ProcessorException("@PostInitialization placed on `${postInitializationMethod.simpleName}` in ${postInitializationMethod.enclosingElement} must not have parameters").setElement(postInitializationMethod)
            }

            if (postInitializationMethod != null && postInitializationMethod.returnType.kind != TypeKind.VOID) {
                throw ProcessorException("@PostInitialization placed on `${postInitializationMethod.simpleName}` in ${postInitializationMethod.enclosingElement} must not have return type").setElement(postInitializationMethod)
            }
            return postInitializationMethod
        }


        fun createTarget(element: TypeElement, dependencyFinder: DependencyTypesFinder): TargetType {
            val type = TargetType(element)

            type.postInitialization = postInitializationMethod(element)
            dependencyFinder.collectSuperTypes(type.element, type.supertypes)

            // get first superclass
            type.superclass?.let {
                type.parentTarget = createTarget(it.asTypeElement(), dependencyFinder)
            }

            val methods = (type.supertypes + listOf(type.element.asType())).methodsWithTargetDependency()
            type.methods.addAll(methods)

            return type
        }

    }

    override fun getSupportedSourceVersion(): SourceVersion? {
        return SourceVersion.latestSupported()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
    }


    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        types = processingEnv.typeUtils
        classesWithDependencyAnnotation.clear()
        methodsWithDependencyAnnotation.clear()

        val dependencies = roundEnv.getElementsAnnotatedWith(Dependency::class.java)
        val libraries = roundEnv.getElementsAnnotatedWith(LibraryModules::class.java)

        dependencies
                .filter { it.isNotMethodAndInterface() }
                .addTo(classesWithDependencyAnnotation)

        dependencies
                .filter { it.kind == ElementKind.METHOD }
                .map { it as ExecutableElement }
                .addTo(methodsWithDependencyAnnotation)

        val alreadyReadModules = mutableSetOf<String>()
        libraries.forEach { findLibraryModules(it.getAnnotation(LibraryModules::class.java), alreadyReadModules) }
        alreadyReadModules.clear()

        measure("Process") {
            try {
                return newParse(roundEnv)
            } catch (e: ProcessorException) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.message, e.element)
            } catch (e: Throwable) {
                throw RuntimeException(e)
            } finally {
                singletons.clear()
                resetUniqueNames()
            }
        }

        return false
    }

    @Throws(ProcessorException::class)
    private fun writeClassFile(packageName: String, classSpec: TypeSpec) {
        val javaFile = JavaFile.builder(packageName, classSpec).addFileComment("This file was generated by Ioc. Do not modify!").build()
        javaFile.writeTo(filer)
    }

    @Throws(Throwable::class)
    override fun throwError(element: Element?, message: String?) {
        throw RuntimeException(message)
    }

    class Type(val typeSpec: TypeSpec, val packageName: String)

    @Throws(Throwable::class)
    fun newParse(roundEnv: RoundEnvironment): Boolean {
        createdSingletones.clear()


        dependencyFinder = DependencyTypesFinder(roundEnv, qualifierFinder)
        dependencyResolver = DependencyResolver(processingEnv.typeUtils, qualifierFinder, dependencyFinder)
        dependencyFinder.dependencyResolver = dependencyResolver

        roundEnv.rootElementsWithInjectedDependencies()
        roundEnv.findDependenciesInParents(processingEnv)

        val targetsWithDependencies = mapToTargetWithDependencies(dependencyResolver)
        val targetTypes = targetsWithDependencies.keys

        // generate singleton classesWithDependencyAnnotation
        val singletons = mutableListOf<SingletonWrapper>()
        val uniqueSingletons = mutableSetOf<String>()
        for (v in targetsWithDependencies.values) {
            SingletonFilter.findAll(v, singletons, uniqueSingletons)
        }


        val rootElements = roundEnv.rootElements
        val typeUtils = processingEnv.typeUtils

        measure("targetWith") {
            for (target in targetsWithDependencies) {
                val dependencies = mutableListOf<DependencyModel>()
                collectAllDependencies(target.value, dependencies)
                val sorting = Sorting()
                sorting.countOrder(" ", target.key.element.asType().toString(), dependencies, 0)
                sorting.sortTargetDependencies(dependencies)

                target.key.dependencies = target.value
                target.key.flatDependencies = dependencies.sortedBy { it.order }.reversed()

                rootElements
                        .firstOrNull { typeUtils.directSupertypes(it.asType()).contains(target.key.element.asType()) }
                        ?.transform { root -> targetTypes.firstOrNull { it.element.isEqualTo(root) } }
                        ?.let { target.key.childTarget = it }
            }

            // set for every parent its own classesWithDependencyAnnotation
            for (target in targetsWithDependencies) {
                for (possibleParent in targetsWithDependencies) {
                    target.key.findParent(possibleParent.key.element.asType())?.let {
                        it.dependencies = possibleParent.key.dependencies
                    }
                }
            }

            for (target in targetTypes) {
                var parentTarget = target.parentTarget
                while (parentTarget != null) {
                    parentTarget = parentTarget.parentTarget
                }
            }
        }

        singletons.map { Type(NewSingletonSpec(it, processingEnv.typeUtils).inject(), it.packageName) }
                .forEach {
                    writeClassFile(it.packageName, it.typeSpec)
                    resetUniqueSingletons()
                }

        // Generate target classesWithDependencyAnnotation
        for (target in targetsWithDependencies) {

            val sorted = target.key.dependencies.sortedBy { it.order }.asReversed()
            val methods = mutableListOf<MethodSpec>()

            for (dependency in sorted) {
                trySetIsFromTarget(target.key, dependency)

                var code = dependencyInjectionCode(dependency, processingEnv.typeUtils, target.key)

                code = wrapInProviderIfNeed(code, dependency)
                code = wrapInLazyIfNeed(code, dependency)
                code.add(wrapInWakIfNeed(dependency))

                val methodBuilder = dependencyInjectionMethod(target.key.className, dependency, code.build())
                injectInTarget(methodBuilder, dependency)
                        .also { methods.add(it) }
            }

            ImplementationsSpec(target.key, processingEnv.typeUtils, methods, target.key.uniqueFlat())
                    .inject()
                    .also { writeClassFile(target.key.className.packageName(), it) }
        }

        return true
    }

    // TODO проверить
    private fun trySetIsFromTarget(target: TargetType, dependency: DependencyModel) {
        if (target.isDeclaredAsMember(dependency)) {
            dependency.isFromTarget = true
        }
    }

    private fun collectAllDependencies(models: List<DependencyModel>, list: MutableList<DependencyModel>) {
        for (model in models) {
            list.add(model)
            for (implementation in model.implementations) {
                collectAllDependencies(implementation.dependencyModels, list)
            }
            for (depencency in model.dependencies) {
                list.add(depencency)
                collectAllDependencies(depencency.dependencies, list)
                for (implementation in depencency.implementations) {
                    collectAllDependencies(implementation.dependencyModels, list)
                }
            }
        }
    }
}
