package com.ioc

import com.ioc.ImplementationsSpec.Companion.addDataObservers
import com.ioc.ImplementationsSpec.Companion.dependencyInjectionCode
import com.ioc.ImplementationsSpec.Companion.provideInjectionMethod
import com.ioc.common.*
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
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
    lateinit var dependencyResolver: DependencyResolver

    companion object {
        val classesWithDependencyAnnotation = mutableListOf<Element>()
        val methodsWithDependencyAnnotation = mutableListOf<ExecutableElement>()
        var singletons = mutableMapOf<String, List<DependencyModel>>()
        var messager: Messager by Delegates.notNull()
        var dependencyFinder: DependencyTypesFinder by Delegates.notNull()
        var processingEnvironment: ProcessingEnvironment by Delegates.notNull()
        val qualifierFinder = QualifierFinder()
        lateinit var types: Types

        fun generateUniqueNamesForInjectMethodDependencies(target: TargetType?, models: List<DependencyModel>) {
            resetUniqueNames()
            val queue = LinkedList(models)

            while (queue.isNotEmpty()) {
                val dep = queue.pop()
                if (!dep.isSingleton &&
                    !target.isSubtype(dep.dependency, dep.originalType) &&
                    !target.isLocalScope(dep.dependency, dep.originalType)) {

                    dep.generatedName = uniqueName(dep)
                }
                if (!dep.isSingleton) queue.addAll(dep.dependencies)
            }
        }

        fun createTarget(element: TypeElement, dependencyFinder: DependencyTypesFinder): TargetType {
            val type = TargetType(element)

            type.postInitialization = postInitializationMethod(element)
            type.dataObservers = findDataObservers(element)
            dependencyFinder.collectSuperTypes(type.element, type.supertypes)

            type.asTargetDependencies.add(element.asType().toString())
            for (supertype in type.supertypes) {
                type.asTargetDependencies.add(supertype.toString())
            }

            // find all localScoped dependencies for use it later
            val injectAnnotationType = processingEnvironment.elementUtils.getTypeElement(LocalScope::class.java.canonicalName)
            val scanner = AnnotationSetScanner(processingEnvironment, mutableSetOf())
            for (localScoped in scanner.scan(element, injectAnnotationType)) {
                val getterName = findDependencyGetter(localScoped).toGetterName()
                type.localScopeDependencies[localScoped.asType().toString()] = getterName
            }

            // get first superclass
            type.superclass?.let {
                type.parentTarget = createTarget(it.asTypeElement(), dependencyFinder)
            }

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
        processingEnvironment = processingEnv
    }


    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        types = processingEnv.typeUtils
        classesWithDependencyAnnotation.clear()
        methodsWithDependencyAnnotation.clear()

        val dependencies = roundEnv.getElementsAnnotatedWith(Dependency::class.java)
        val libraries = roundEnv.getElementsAnnotatedWith(LibraryModules::class.java)

        for (dependency in dependencies) {
            if (dependency.isNotMethodAndInterface()) {
                classesWithDependencyAnnotation.add(dependency)
                continue
            }

            if (dependency.kind == ElementKind.METHOD) {
                methodsWithDependencyAnnotation.add(dependency as ExecutableElement)
            }
        }

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
        val iocVersion = "1.1.9"
        val javaFile = JavaFile.builder(packageName, classSpec).addFileComment("This file was generated by Ioc $iocVersion. Do not modify!").build()
        javaFile.writeTo(filer)
    }

    @Throws(Throwable::class)
    override fun throwError(element: Element?, message: String?) {
        throw RuntimeException(message)
    }

    @Throws(Throwable::class)
    fun newParse(roundEnv: RoundEnvironment): Boolean {
        dependencyFinder = DependencyTypesFinder(qualifierFinder)
        dependencyResolver = DependencyResolver(processingEnv.typeUtils, qualifierFinder, dependencyFinder)
        dependencyFinder.dependencyResolver = dependencyResolver

        roundEnv.rootElementsWithInjectedDependencies()
        roundEnv.findDependenciesInParents(processingEnv)

        val targetsWithDependencies = mapToTargetWithDependencies(dependencyResolver)
        val targetTypes = targetsWithDependencies.keys


        validateSingletonUsage(targetsWithDependencies)

        // generate singleton classesWithDependencyAnnotation
        val uniqueSingletons = mutableSetOf<String>()
        val singletons = mutableListOf<DependencyModel>()
        for (v in targetsWithDependencies.values) {
            SingletonFilter.findAll(v, singletons, uniqueSingletons)
        }

        for (target in targetsWithDependencies) {
            val dependencies = mutableListOf<DependencyModel>()
            collectAllDependencies(target.value, dependencies)
            val sorting = Sorting()
            sorting.countOrder(" ", target.key.element.asType().toString(), dependencies, 0)
            sorting.sortTargetDependencies(dependencies)

            target.key.dependencies = target.value
        }

        // set for every parent its own classesWithDependencyAnnotation
        for (target in targetsWithDependencies) {
            for (possibleParent in targetsWithDependencies) {
                target.key.findParent(possibleParent.key.element.asType())?.let {
                    it.dependencies = possibleParent.key.dependencies
                }
            }
        }

        applyTargetParents(targetTypes)
        createSingletons(singletons)
        generateInjectableSpecs(targetsWithDependencies)

        return true
    }

    private fun applyTargetParents(targetTypes: Set<TargetType>) {
        for (target in targetTypes) {
            var parentTarget = target.parentTarget
            while (parentTarget != null) {
                parentTarget = parentTarget.parentTarget
            }
        }
    }

    private fun createSingletons(singletons: MutableList<DependencyModel>) {
        for (singleton in singletons) {
            generateUniqueNamesForInjectMethodDependencies(null, singleton.dependencies)
            val spec = NewSingletonSpec(singleton)
            writeClassFile(singleton.originalType.getPackage().toString(), spec.inject())
        }
    }

    private fun generateInjectableSpecs(targets: Map<TargetType, MutableList<DependencyModel>>) {
        for (target in targets) {

            val sorted = target.key.dependencies.sortedByDescending { it.sortOrder }
            val methods = mutableListOf<InjectMethod>()

            val singletonsToInject = mutableListOf<DependencyModel>()
            val emptyConstructorToInject = mutableListOf<DependencyModel>()
            val emptyModuleMethodToInject = mutableListOf<DependencyModel>()
            for (dependency in sorted) {
                if (dependency.isSingleton) {
                    singletonsToInject.add(dependency)
                    continue
                }

                if (dependency.isAllowEmptyConstructorInjection()) {
                    emptyConstructorToInject.add(dependency)
                    continue
                }

                if (dependency.isAllowModuleMethodProvide()) {
                    emptyModuleMethodToInject.add(dependency)
                    continue
                }

                generateUniqueNamesForInjectMethodDependencies(target.key, dependency.dependencies)
                val code = dependencyInjectionCode(target.key, dependency)
                // generate base injection code
                val isTargetUsedAsDependency = isTargetUsedWhileCreateDependency(target.key, dependency)
                val methodBuilder = provideInjectionMethod(target.key.className, isTargetUsedAsDependency, dependency, code.build())
                methods.add(InjectMethod(methodBuilder.build(), isTargetUsedAsDependency, dependency))
            }

            methods.addAll(addDataObservers(target.key))
            val typeSpec = ImplementationsSpec(target.key, methods).inject(singletonsToInject, emptyConstructorToInject, emptyModuleMethodToInject)
            writeClassFile(target.key.className.packageName(), typeSpec)
        }
    }

    private fun isTargetUsedWhileCreateDependency(target: TargetType, dependency: DependencyModel): Boolean {
        if (dependency.isViewModel) return true
        if (target.isSubtype(dependency.dependency, dependency.originalType)) return true
        val queue = LinkedList<DependencyModel>()

        queue.addAll(dependency.dependencies)
        while (queue.isNotEmpty()) {
            val dep = queue.pop()
            if (target.isSubtype(dep.dependency, dep.originalType) ||
                target.isLocalScope(dep.dependency, dep.originalType) ||
                dep.isViewModel) {
                return true
            }
            queue.addAll(dep.dependencies)
        }
        return false
    }

    // TODO remove recursive
    private fun collectAllDependencies(models: List<DependencyModel>, list: MutableList<DependencyModel>) {
        for (model in models) {
            list.add(model)
            model.methodProvider?.let { collectAllDependencies(it.dependencyModels, list) }
            for (dependency in model.dependencies) {
                list.add(dependency)
                collectAllDependencies(dependency.dependencies, list)
                dependency.methodProvider?.let { collectAllDependencies(it.dependencyModels, list) }
            }
        }
    }
}
