package com.ioc

import com.ioc.ImplementationsSpec.Companion.addDataObservers
import com.ioc.ImplementationsSpec.Companion.dependencyInjectionCode
import com.ioc.ImplementationsSpec.Companion.provideInjectionMethod
import com.ioc.common.*
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import java.util.*
import javax.annotation.processing.*
import javax.inject.Singleton
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
    lateinit var dependencyResolver: DependencyResolver

    companion object {
        var filer: Filer by Delegates.notNull()
        val classesWithDependencyAnnotation = mutableListOf<Element>()
        val methodsWithDependencyAnnotation = mutableListOf<ExecutableElement>()
        val projectSingletons = mutableMapOf<String, DependencyModel>()
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
            type.supertypes.addAll(dependencyFinder.collectSuperTypes(type.element))

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

        measure("Ioc Annotation Processing") {
            try {
                return newParse(roundEnv)
            } catch (e: ProcessorException) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.message, e.element)
            } catch (e: Throwable) {
                throw RuntimeException(e)
            } finally {
                dependencyResolver.cachedConstructorArguments.clear()
                projectSingletons.clear()
                resetUniqueNames()
            }
        }

        return false
    }

    @Throws(Throwable::class)
    override fun throwError(element: Element?, message: String?) {
        throw RuntimeException(message)
    }

    @Throws(Throwable::class)
    fun newParse(roundEnv: RoundEnvironment): Boolean {
        dependencyFinder = DependencyTypesFinder(qualifierFinder)
        dependencyResolver = DependencyResolver(qualifierFinder, dependencyFinder)
        dependencyFinder.dependencyResolver = dependencyResolver

        roundEnv.rootElementsWithInjectedDependencies()
        roundEnv.findDependenciesInParents(processingEnv)

        val targetsWithDependencies = mapToTargetWithDependencies(dependencyResolver)
        val targetTypes = targetsWithDependencies.keys

        val singletonElements = roundEnv.getElementsAnnotatedWith(Singleton::class.java)
        for (singletonElement in singletonElements) {
            validateSingletonClass(singletonElement)
            validateSingletonMethod(singletonElement)
            var element = singletonElement
            if (singletonElement.isMethod()) element = singletonElement.asMethod().returnType.asElement()
            if (projectSingletons.containsKey(element.asTypeString())) continue
            val t = processingEnvironment.elementUtils.getTypeElement(SingletonStorage::class.java.canonicalName)
            dependencyResolver.resolveDependency(element, TargetType(t))
        }

        for (target in targetsWithDependencies) {
            val dependencies = collectAllDependencies(target.value)
            //collectAllDependencies(target.value)
            val sorting = Sorting()
            sorting.countOrder(" ", target.key.element.asTypeString(), dependencies, 0)
            sorting.sortTargetDependencies(dependencies)
            target.key.dependencies = target.value
        }


        // find for each target first it's parent target with dependencies and apply dependencies
        for (target in targetsWithDependencies) {
            for (possibleParent in targetsWithDependencies) {
                if (possibleParent.key.dependencies.isEmpty()) continue
                target.key.findParent(possibleParent.key.element.asType())?.let {
                    it.dependencies = possibleParent.key.dependencies
                }
            }
        }

        applyTargetParents(targetTypes)
        createSingletons(projectSingletons.values)
        generateInjectableSpecs(targetsWithDependencies)

        if (targetTypes.isNotEmpty()) {
            TargetFactorySpec.createSpec(targetTypes).writeClass("com.ioc")
        }

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

    private fun createSingletons(singletons: Collection<DependencyModel>) {
        for (singleton in singletons) {
            generateUniqueNamesForInjectMethodDependencies(null, singleton.dependencies)
            val spec = NewSingletonSpec(singleton)
            spec.createSpec().writeClass(singletonClassPackage(singleton))
        }

        if (singletons.isEmpty()) return
        SingletonFactorySpec.createSpec(singletons).writeClass("com.ioc")
    }

    class CachedMethod(val classTypeName: TypeName, val methodSpec: MethodSpec)
    private fun generateInjectableSpecs(targets: Map<TargetType, MutableList<DependencyModel>>) {
        val cachedGeneratedMethods = mutableMapOf<String, CachedMethod>()
        for (target in targets) {

            val sorted = target.key.dependencies.sortedByDescending { it.sortOrder }
            val methods = mutableListOf<InjectMethod>()

            val singletonsToInject = mutableListOf<DependencyModel>()
            val emptyConstructorToInject = mutableListOf<DependencyModel>()
            val emptyModuleMethodToInject = mutableListOf<DependencyModel>()
            val fromDifferentModuleInject = mutableListOf<InjectMethod>()
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

                val named = if (dependency.named != null) dependency.named!! else ""
                val key = "$named${dependency.originalTypeString}"
                val isTargetUsedAsDependency = isTargetUsedWhileCreateDependency(target.key, dependency)

                if (cachedGeneratedMethods.containsKey(key)) {
                    val cache = cachedGeneratedMethods.getValue(key)
                    val methodCode = cache.methodSpec.code
                    if (isTargetUsedAsDependency) {
                        methods.add(InjectMethod(MethodSpec.methodBuilder(cache.methodSpec.name)
                            .addParameter(targetParameter(target.key.className))
                            .returns(cache.methodSpec.returnType)
                            .addModifiers(cache.methodSpec.modifiers)
                            .addAnnotation(keepAnnotation)
                            .addCode(methodCode)
                            .build(), isTargetUsedAsDependency, dependency, cache.classTypeName))
                    }
                    else if (!isTargetUsedAsDependency && dependency.isNotGeneric()) {
                        fromDifferentModuleInject.add(InjectMethod(cache.methodSpec, isTargetUsedAsDependency, dependency, cache.classTypeName))
                    }
                    continue
                }

                generateUniqueNamesForInjectMethodDependencies(target.key, dependency.dependencies)
                val code = dependencyInjectionCode(target.key, dependency)
                val methodBuilder = provideInjectionMethod(target.key.className, isTargetUsedAsDependency, dependency, code.build())
                val builtMethod = methodBuilder.build()
                val method = InjectMethod(builtMethod, isTargetUsedAsDependency, dependency, targetInjectionTypeName(target.key))
                methods.add(method)
                cachedGeneratedMethods[key] = CachedMethod(targetInjectionTypeName(target.key), builtMethod)
            }

            methods.addAll(addDataObservers(target.key))
            val typeSpec = ImplementationsSpec(target.key, methods).inject(singletonsToInject, emptyConstructorToInject, emptyModuleMethodToInject, fromDifferentModuleInject)
            typeSpec.writeClass(targetInjectionPackage(target.key))
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

    private fun collectAllDependencies(dependencies: List<DependencyModel>): MutableList<DependencyModel> {
        val allTargetDependencies = mutableListOf<DependencyModel>()
        val queue = LinkedList(dependencies)
        while (queue.isNotEmpty()) {
            val dep = queue.pop()
            allTargetDependencies.add(dep)
            queue.addAll(dep.dependencies)
        }
        return allTargetDependencies
    }
}
