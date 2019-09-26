package com.ioc

import com.ioc.IProcessor.Companion.qualifierFinder
import com.ioc.common.*
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

class DependencyTypesFinder(
    private val qualifierFinder: QualifierFinder) {

    lateinit var dependencyResolver: DependencyResolver

    @Throws(ProcessorException::class)
    fun findFor(
        element: Element,
        named: String?,
        target: TargetType,
        typeElement: Element): ModuleMethodProvider? {

        val isInterface = typeElement.isInterface()
        val isAbstractClass = typeElement.isAbstract()



        // Try find dependency by annotated @Dependency method
        val methodProvider = findNotAbstractModuleMethodProvider(element, named, target)
        methodProvider?.let { return it }

        if (!isInterface && !isAbstractClass) return null

        if ((isInterface || isAbstractClass) && methodProvider == null) {
            throwsCantFindImplementations(element, target)
        }

        return null
    }

    private fun findClassWithDependencyAnnotation(element: Element, named: String?): DependencyModel? {
        val implementations = mutableListOf<DependencyModel>()
        for (implementation in classesWithDependencyAnnotation()) {
            // is the same type skip it
            if (element.isInterface() && element.isEqualTo(implementation)) continue
            val implementationType = implementation.asTypeElement()
            if (isSubtype(element, implementationType, named)) {
                implementations.add(DependencyModel(
                    dependency = element,
                    originalType = implementationType,
                    isSingleton = implementationType.isSingleton(),
                    isLocal = implementationType.isLocalScoped()
                ))
                throwsMoreThanOneDependencyFoundIfNeed(element, named, implementations.map { it.originalTypeString })
            }
        }
        return implementations.firstOrNull()
    }

    private fun findAbstractModuleMethodProvider(element: Element, named: String?): DependencyModel? {
        val implementations = mutableListOf<DependencyModel>()
        for (method in methodsWithDependencyAnnotation()) {
            if (!method.isAbstract()) continue
            val type = method.returnType.asTypeElement()

            if (!isSubtype(element, type, named)) continue

            validateAbstractModuleMethodProvider(method)

            // method must contain only one parameter and must be interface
            val implementationType = method.parameters.firstOrNull()?.asTypeElement()

            implementations.add(DependencyModel(
                dependency = element,
                originalType = implementationType ?: type,
                isSingleton = method.isSingleton(),
                isLocal = method.isLocalScoped()
            ))
            throwsMoreThanOneDependencyFoundIfNeed(element, named, implementations.map { it.originalTypeString })
        }
        return implementations.firstOrNull()
    }

    fun findImplementationFromAbstractModuleMethodProviderOrFromClassWithDependencyAnnotation(element: Element, named: String?): DependencyModel? {
        findAbstractModuleMethodProvider(element, named)?.let { return it }
        findClassWithDependencyAnnotation(element, named)?.let { return it }
        return null
    }

    private fun findNotAbstractModuleMethodProvider(element: Element, named: String?, target: TargetType): ModuleMethodProvider? {
        val implementations = mutableListOf<ModuleMethodProvider>()
        for (provider in methodsWithDependencyAnnotation()) {
            if (provider.isAbstract()) continue
            val isKotlinModule = isModuleKotlinObject(provider.enclosingElement.asTypeElement())

            validateModuleMethod(isKotlinModule, provider)

            if (!provider.returnType.isEqualTo(element) || !qualifierFinder.hasNamed(named, provider)) continue

            val returnType = provider.returnType.asTypeElement()

            val type = createModuleMethodProvider(returnType, provider.enclosingElement, target, provider.parameters)
            type.isKotlinModule = isKotlinModule
            type.name = provider.simpleName
            type.isSingleton = type.isSingleton || provider.isSingleton()
            type.isLocal = type.isLocal || provider.isLocalScoped()
            if (type.isSingleton) throwsIfTargetUsedInSingleton(target, provider, type.dependencies)
            if (type.isSingleton && returnType.isAbstract() && qualifierFinder.getQualifier(provider) != null)
                throwsSingletonMethodAbstractReturnType(provider)
            implementations.add(type)
            throwsMoreThanOneDependencyFoundIfNeed(element, named, implementations.map { "${it.module.simpleName}.${it.name}" })
        }

        return implementations.firstOrNull()
    }

    private fun createModuleMethodProvider(
        implementation: TypeElement,
        module: Element,
        target: TargetType,
        methodParameters: List<Element>): ModuleMethodProvider {

        val named = qualifierFinder.getQualifier(implementation)
        val provider = ModuleMethodProvider(implementation.simpleName, module, named)

        if (methodParameters.isEmpty()) return provider
        for (dependency in methodParameters) {
            if (target.isLocalScope(dependency, dependency) || target.isSubtype(dependency, dependency)) {
                provider.dependencies.add(targetDependencyModel(dependency))
                continue
            }

            if (dependency.isEqualTo(implementation.asType())) {
                throw ProcessorException("Cyclic graph detected building ${dependency.asType()} cyclic: ${dependency.asType()}").setElement(implementation)
            }
            val resolved = dependencyResolver.resolveDependency(dependency, target = target)
            provider.dependencies.add(resolved)
        }

        return provider
    }


    fun collectSuperTypes(typeElement: TypeElement?): Set<TypeMirror> {
        typeElement ?: return emptySet()

        val supertypes = mutableSetOf<TypeMirror>()
        val queue = LinkedList<TypeMirror>()
        queue.addAll(typeElement.interfaces)
        queue.add(typeElement.superclass)

        while (queue.isNotEmpty()) {
            val supertype = queue.pop()
            if (supertype.isNotValid()) continue
            supertypes.add(supertype)
            val superclass = supertype.asTypeElement()
            queue.addAll(superclass.interfaces)
            queue.add(superclass.superclass)
        }

        return supertypes
    }

    companion object {
        // We have a dependency that needs to check with all superclasses and their interfaces
        fun isSubtype(dependency: Element, implementation: TypeElement, named: String? = null): Boolean {
            return compareSuperclasses(dependency, named, implementation.asType())
        }

        private fun compareSuperclasses(compareElement: Element, named: String?, superclass: TypeMirror?): Boolean {
            superclass ?: return false
            if (superclass.isNotValid()) return false
            val typeElement = superclass.asTypeElement()
            val typeElementNamed = qualifierFinder.getQualifier(typeElement)
            if (typeElement.isEqualTo(compareElement) && named == typeElementNamed) return true
            if (typeElement.interfaces.any { compareInterfaces(compareElement, it) } && named == typeElementNamed) return true
            return compareSuperclasses(compareElement, named, typeElement.superclass)
        }

        private fun compareInterfaces(compareElement: Element, checkingInterface: TypeMirror?): Boolean {
            checkingInterface ?: return false
            if (checkingInterface.isEqualTo(compareElement)) return true
            return checkingInterface.asTypeElement().interfaces.any { compareInterfaces(compareElement, it) }
        }
    }

}