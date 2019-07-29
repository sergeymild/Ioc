package com.ioc

import com.ioc.ImplementationsSpec.Companion.dependencyInjectionCode
import com.ioc.ImplementationsSpec.Companion.dependencyInjectionMethod
import com.ioc.ImplementationsSpec.Companion.injectInTarget
import com.ioc.common.*
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.util.*
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
    lateinit var dependencyResolver: DependencyResolver

    companion object {
        val classesWithDependencyAnnotation = mutableListOf<Element>()
        val methodsWithDependencyAnnotation = mutableListOf<ExecutableElement>()
        var singletons = mutableMapOf<String, MutableList<DependencyModel>>()
        var messager: Messager by Delegates.notNull()
        var dependencyFinder: DependencyTypesFinder by Delegates.notNull()
        var processingEnvironment: ProcessingEnvironment by Delegates.notNull()
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
        val singletons = mutableListOf<SingletonWrapper>()
        val uniqueSingletons = mutableSetOf<String>()
        for (v in targetsWithDependencies.values) {
            SingletonFilter.findAll(v, singletons, uniqueSingletons)
        }

        measure("targetWith") {
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

            for (target in targetTypes) {
                var parentTarget = target.parentTarget
                while (parentTarget != null) {
                    parentTarget = parentTarget.parentTarget
                }
            }
        }

        for (singleton in singletons) {
            val spec = NewSingletonSpec(singleton, processingEnv.typeUtils)
            writeClassFile(singleton.packageName, spec.inject())
        }

        // Generate target classesWithDependencyAnnotation
        for (target in targetsWithDependencies) {

            val sorted = target.key.dependencies.sortedByDescending { it.sortOrder }//.asReversed()
            val methods = mutableListOf<MethodSpec>()

            for (dependency in sorted) {
                // generate base injection code
                val code = dependencyInjectionCode(dependency, processingEnv.typeUtils, target.key)
                val methodBuilder = dependencyInjectionMethod(target.key.className, dependency, code.build())
                injectInTarget(methodBuilder, dependency)
                    .also { methods.add(it) }
            }

            ImplementationsSpec(target.key, methods).inject()
                .also { writeClassFile(target.key.className.packageName(), it) }
        }

        return true
    }

    private fun validateSingletonUsage(
        targetsWithDependencies: Map<TargetType, MutableList<DependencyModel>>) {
        // check how ofter used singletons
        val counter = mutableMapOf<String, Int>()
        for (target in targetsWithDependencies) {
            val queue = LinkedList(target.value)
            while (queue.isNotEmpty()) {
                val dep = queue.pop()
                if (dep.isSingleton) {
                    val count = counter.getOrPut(dep.typeElementString) { 0 }
                    counter[dep.typeElementString] = count + 1
                }
                queue.addAll(dep.dependencies)
            }
        }

        for (entry in counter) {
            if (entry.value == 1) message("@Singleton is redundant for dependency: ${entry.key}")
        }
    }

    // TODO isParentDependencySingleton
    private fun collectUsedSingletonsInMethodCreation(dependencies: List<DependencyModel>, isParentDependencySingleton: Boolean): MutableMap<String, DependencyModel> {
        // try to find all singleton used in creation of current inject
        var isLocalParentDependencySingleton = isParentDependencySingleton
        val usedSingletons = mutableMapOf<String, DependencyModel>()
        val queue = LinkedList(dependencies)
        while (queue.isNotEmpty()) {
            val dep = queue.pop()
            val key = dep.typeElement.asType().toString()
            if (dep.isSingleton && !usedSingletons.containsKey(key) && !isLocalParentDependencySingleton) {
                usedSingletons[key] = dep
                continue
            }
            isLocalParentDependencySingleton = dep.isSingleton
            if (!dep.isSingleton) queue.addAll(dep.dependencies)
        }
        return usedSingletons
    }

    private fun collectAllDependencies(models: List<DependencyModel>, list: MutableList<DependencyModel>) {
        for (model in models) {
            list.add(model)
            for (implementation in model.implementations) {
                collectAllDependencies(implementation.dependencyModels, list)
            }
            for (dependency in model.dependencies) {
                list.add(dependency)
                collectAllDependencies(dependency.dependencies, list)
                for (implementation in dependency.implementations) {
                    collectAllDependencies(implementation.dependencyModels, list)
                }
            }
        }
    }

    private fun isNeedPassTarget(dependency: DependencyModel): Boolean {
        val queue = LinkedList(dependency.dependencies)
        while (queue.isNotEmpty()) {
            val dep = queue.pop()
            if (dep.asTarget) return true
            queue.addAll(dep.dependencies)
        }
        return false
    }
}
