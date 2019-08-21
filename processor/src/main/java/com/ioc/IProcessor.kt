package com.ioc

import com.ioc.ImplementationsSpec.Companion.addDataObservers
import com.ioc.ImplementationsSpec.Companion.dependencyInjectionCode
import com.ioc.ImplementationsSpec.Companion.provideInjectionMethod
import com.ioc.common.*
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.properties.Delegates


open class IProcessor : AbstractProcessor() {
    lateinit var dependencyResolver: DependencyResolver

    companion object {
        var filer: Filer by Delegates.notNull()
        var elementUtils: Elements by Delegates.notNull()
        val classesWithDependencyAnnotation = mutableListOf<Element>()
        val methodsWithDependencyAnnotation = mutableListOf<ExecutableElement>()
        val projectSingletons = mutableMapOf<String, DependencyModel>()
        var messager: Messager by Delegates.notNull()
        var dependencyFinder: DependencyTypesFinder by Delegates.notNull()
        var processingEnvironment: ProcessingEnvironment by Delegates.notNull()
        val qualifierFinder = QualifierFinder()
        lateinit var types: Types
    }

    override fun getSupportedSourceVersion(): SourceVersion? {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
            Inject::class.java.canonicalName,
            Singleton::class.java.canonicalName,
            Dependency::class.java.canonicalName
        )
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
        processingEnvironment = processingEnv
        elementUtils = processingEnv.elementUtils
    }


    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        types = processingEnv.typeUtils
        classesWithDependencyAnnotation.clear()
        methodsWithDependencyAnnotation.clear()

        val dependencies = roundEnv.getElementsAnnotatedWith(Dependency::class.java)

        for (dependency in dependencies) {
            if (dependency.isNotMethodAndInterface()) {
                classesWithDependencyAnnotation.add(dependency)
                continue
            }

            if (dependency.isMethod()) {
                methodsWithDependencyAnnotation.add(dependency as ExecutableElement)
            }
        }

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
    fun newParse(roundEnv: RoundEnvironment): Boolean {
        dependencyFinder = DependencyTypesFinder(qualifierFinder)
        dependencyResolver = DependencyResolver(qualifierFinder, dependencyFinder)
        dependencyFinder.dependencyResolver = dependencyResolver

        val targetDependencies = mutableMapOf<String, MutableSet<Element>>()
        val rootTypeElements = mutableListOf<TypeElement>()

        measure("rootElementsWithInjectedDependencies") {
            roundEnv.rootElementsWithInjectedDependencies(targetDependencies, rootTypeElements)
        }

        val targetsWithDependencies = mapToTargetWithDependencies(dependencyResolver, targetDependencies, rootTypeElements)
        val targetTypes = targetsWithDependencies.keys

        val singletonElements = roundEnv.getElementsAnnotatedWith(Singleton::class.java)
        for (singletonElement in singletonElements) {
            validateSingletonClass(singletonElement)
            validateSingletonMethod(singletonElement)
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
                    } else {
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
