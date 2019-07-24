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
import javax.inject.Scope
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
        var scopeTargets = mutableMapOf<String, MutableMap<TargetType, MutableSet<String>>>()
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

            type.isRootScope = element.isRootScope()
            type.isNestedScope = element.isChildScope()
            if (type.isRootScope) {
                type.rootScope = ScopeFinder.getScope(element) ?: ROOT_SCOPE
            }

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

        roundEnv.getElementsAnnotatedWith(Dependency::class.java)
                .filter { it.isNotMethodAndInterface() }
                .addTo(classesWithDependencyAnnotation)

        roundEnv.getElementsAnnotatedWith(Dependency::class.java)
                .filter { it.kind == ElementKind.METHOD }
                .map { it as ExecutableElement }
                .addTo(methodsWithDependencyAnnotation)

        val alreadyReadModules = mutableSetOf<String>()
        roundEnv.getElementsAnnotatedWith(LibraryModules::class.java)
                .forEach { findLibraryModules(it.getAnnotation(LibraryModules::class.java), alreadyReadModules) }
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



        message("-> Get root elements")
        val rootTypeElements = roundEnv.rootElementsWithInjectedDependencies()

        message("-> Map to target with dependencies")
        val targetsWithDependencies = mapToTargetWithDependencies(rootTypeElements, dependencyResolver)
        message("-> Done")
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
                    parentTarget.rootScope = target.rootScope
                    parentTarget = parentTarget.parentTarget
                }
            }
        }

        singletons.map { Type(NewSingletonSpec(it, processingEnv.typeUtils).inject(), it.packageName) }
                .forEach {
                    writeClassFile(it.packageName, it.typeSpec)
                    resetUniqueSingletons()
                }


        for (scopeAnnotation in roundEnv.getElementsAnnotatedWith(Scope::class.java)) {
            for (scopedElement in roundEnv.getElementsAnnotatedWith(scopeAnnotation.asTypeElement())) {
                if (scopedElement.kind == ElementKind.PARAMETER) continue
                if (scopedElement.isRootScope()) {
                    val rootScopeName = scopeAnnotation.simpleName.toString()
                    scopeTargets[rootScopeName] = mutableMapOf()
                    targetTypes.firstOrNull { it.element.isEqualTo(scopedElement) }?.let {
                        scopeTargets[rootScopeName]!![it] = mutableSetOf()
                    }
                    continue
                }
                if (ScopeFinder.isKotlinAnnotationsMethod(scopedElement)) continue
                if (scopedElement.isInterface()) {
                    throw ProcessorException("`@${scopeAnnotation.simpleName}` must be declared on one of the implementations `$scopedElement`").setElement(scopedElement)
                }

                // TODO!!!!
                scopeTargets[scopeAnnotation.simpleName.toString()]?.keys?.first()?.let {
                    scopeTargets[scopeAnnotation.simpleName.toString()]!![it]?.add(scopedElement.asType().toString())
                }
                val targets = targetTypes.filter { it.isTargetForDependency(scopeAnnotation, scopedElement) }
                if (targets.isEmpty()) {
                    message("Can't find Element annotated with `@ScopeRoot(@$scopeAnnotation.class)`")
                }

                for (target in targets) {
                    // target must have this dependency
                    val contains = target.flatDependencies.any { it.dependency.isEqualTo(scopedElement) && it.scoped == target.rootScope }
                    //if (!contains) throw ProcessorException("Dependency with `@${scopeAnnotation.simpleName}` not found").setElement(scopedElement)
                    if (!contains) {
                        message("Dependency `${scopedElement.simpleName}` with `@${scopeAnnotation.simpleName}` not used in `${target.element.simpleName}`")
                        continue
                    }

                    target.parentsDependencies()
                    (target.parentDependencies + target.flatDependencies)
                            .filter { it.dependency.isEqualTo(scopedElement) && it.scoped == target.rootScope }
                            .forEach {
                                it.isFromTarget = true
                                it.scoped = target.rootScope
                                it.scopedFieldName = "${target.rootScope.decapitalize()}${it.scopedFieldName.capitalize()}"
                            }
                }
            }
        }

        val cacheMethods = mutableMapOf<TargetType, MutableList<MethodSpec>>()
        for (pair in scopeTargets) {
            for (entry in pair.value) {
                for (type in entry.value) {
                    val scopeHolder = targetTypes.filter { it.rootScope == pair.key }.map { it }.firstOrNull()
                    val dependency = targetsWithDependencies.flatMap { it.value }.firstOrNull { it.typeElement.asType().toString() == type }
                            ?: continue
                    val method = ImplementationsSpec.cachedMethod(scopeHolder!!, typeUtils, dependency)
                    cacheMethods.getOrPut(scopeHolder) { mutableListOf() }.add(method)
                }
            }
        }

        // Generate target classesWithDependencyAnnotation
        for (target in targetsWithDependencies) {

            val uniqueFlatDependencies = target.key.uniqueFlat()
                    .asSequence()
                    .filter { it.isFromTarget }
                    .filter { it.scoped != null && it.scoped != ROOT_SCOPE }
                    .filter { !target.key.parentDependencies.contains(it) }
                    .filter { !target.key.dependencies.contains(it) }
                    .toMutableSet()

            val prevRootScope = target.key.rootScope
            if (prevRootScope == ROOT_SCOPE) {
                var child = target.key.childTarget
                while (child != null) {
                    target.key.rootScope = child.rootScope
                    if (target.key.rootScope != ROOT_SCOPE) break
                    child = child.childTarget
                }
            }

            val sorted = target.key.dependencies.sortedBy { it.order }.asReversed()
            val methods = mutableListOf<MethodSpec>()

            cacheMethods[target.key]?.let {
                it.forEachIfNotNull { method -> methods.add(method) }
            }

            for (dependency in sorted) {
                scopedFieldName(target.key, dependency)
                trySetIsFromTarget(target.key, dependency)

                var code = dependencyInjectionCode(dependency, processingEnv.typeUtils, target.key)

                code = wrapInProviderIfNeed(code, dependency)
                code = wrapInLazyIfNeed(code, dependency)
                code.add(wrapInWakIfNeed(dependency))

                val methodBuilder = dependencyInjectionMethod(target.key.className, dependency, code.build())
                injectInTarget(methodBuilder, dependency)
                        .also { methods.add(it) }

                //injectBaseSpeedDialItemsPresenterInBaseSpeedDialItemsPresenter

                val cachedDependencies = uniqueFlatDependencies.filter { it.order <= dependency.order }
                for (cachedDependency in cachedDependencies) {
                    uniqueFlatDependencies.remove(cachedDependency)
                    ImplementationsSpec.cachedMethod(target.key, typeUtils, cachedDependency)
                            .also { methods.add(it) }
                }
            }

//            val cacheMethods = mutableListOf<MethodSpec>()
//            for (remainCacheDependency in uniqueFlatDependencies) {
//                ImplementationsSpec.cachedMethod(target.key, typeUtils, remainCacheDependency)
//                        .also { cacheMethods.add(it) }
//            }

//            methods.addAll(0, cacheMethods)

            ImplementationsSpec(target.key, processingEnv.typeUtils, methods, target.key.uniqueFlat())
                    .inject()
                    .also { writeClassFile(target.key.className.packageName(), it) }

            target.key.rootScope = prevRootScope
        }

        return true
    }

    // TODO проверить
    private fun trySetIsFromTarget(target: TargetType, dependency: DependencyModel) {
        if (target.isDeclaredAsMember(dependency)) {
            dependency.isFromTarget = true
        }
    }

    private fun scopedFieldName(target: TargetType, dependency: DependencyModel) {
        dependency.scopedFieldName = "${target.rootScope.decapitalize()}${dependency.dependency.asTypeElement().simpleName.toString().capitalize()}"
    }

//    private fun generateUniqueNames(dependency: DependencyModel) {
//        //dependency.generatedName = uniqueName(dependency.name)
//        for (dep in dependency.depencencies) {
//            generateUniqueNames(dep)
//        }
//    }

    private fun collectAllDependencies(models: List<DependencyModel>, list: MutableList<DependencyModel>) {
        for (model in models) {
            list.add(model)
            for (implementation in model.implementations) {
                collectAllDependencies(implementation.dependencyModels, list)
            }
            for (depencency in model.depencencies) {
                list.add(depencency)
                collectAllDependencies(depencency.depencencies, list)
                for (implementation in depencency.implementations) {
                    collectAllDependencies(implementation.dependencyModels, list)
                }
            }
        }
    }
}
