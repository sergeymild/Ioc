package com.ioc

import com.ioc.IProcessor.Companion.qualifierFinder
import com.ioc.common.*
import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
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
        typeElement: Element): List<DependencyProvider> {

        val isInterface = typeElement.isInterface()
        val isAbstractClass = typeElement.isAbstract()

        val implementations = mutableListOf<DependencyProvider>()

        // Try find dependency by annotated @Dependency method
        findNotAbstractModuleMethodProvider(element, named, typeElement, target, implementations)

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@$named"}] founded [${implementations.joinToString { it.method.asType().toString() }}]")
                .setElement(element)
        }

        if (implementations.isNotEmpty()) {
            return implementations
        }

        if (!isInterface && !isAbstractClass) return emptyList()

        if (implementations.isNotEmpty()) return implementations

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@" + (if (named == null) "Default" else named)}]")
                .setElement(element)
        }

        if (implementations.isNotEmpty()) {
            return implementations
        }

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@" + (if (named == null) "Default" else named)}]")
                .setElement(element)
        }

        if ((isInterface || isAbstractClass) && implementations.isEmpty()) {
            throw ProcessorException("Can't find implementations of `${element.asType()} ${element.enclosingElement}` forTarget: ${target.element} maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method.").setElement(element)
                .setElement(element)
        }

        return implementations
    }

    fun findClassWithDependencyAnnotation(element: Element, named: String?): DependencyModel? {
        val implementations = mutableListOf<DependencyModel>()
        for (implementation in classesWithDependencyAnnotation()) {
            // is the same type skip it
            if (element.isInterface() && element.isEqualTo(implementation)) continue
            val implementationType = implementation.asTypeElement()
            if (isSubtype(element, implementationType, named)) {
                implementations.add(DependencyModel(
                    dependency = element,
                    originalType = implementationType,
                    //fieldName = element.simpleName,
                    isSingleton = implementationType.isSingleton()
                ))
                throwMoreThanOneDependencyFoundIfNeed(element, named, implementations)
            }
        }
        return implementations.firstOrNull()
    }

    fun findAbstractModuleMethodProvider(element: Element, named: String?): DependencyModel? {
        val implementations = mutableListOf<DependencyModel>()
        for (method in abstractMethodsWithDependencyAnnotations()) {
            val type = method.returnType.asTypeElement()
            if (!isSubtype(element, type, named)) continue

            validateAbstractModuleMethodProvider(method)

            // method must contain only one parameter and must be interface
            val implementationType = method.parameters.firstOrNull()?.asTypeElement()

            implementations.add(DependencyModel(
                dependency = element,
                originalType = implementationType ?: type,
                //fieldName = element.simpleName,
                isSingleton = method.isSingleton()
            ))
            throwMoreThanOneDependencyFoundIfNeed(element, named, implementations)
        }
        return implementations.firstOrNull()
    }

    fun findImplementationFromAbstractModuleMethodProviderOrFromClassWithDependencyAnnotation(element: Element, named: String?): DependencyModel? {
        findAbstractModuleMethodProvider(element, named)?.let { return it }
        findClassWithDependencyAnnotation(element, named)?.let { return it }
        return null
    }

    private fun findNotAbstractModuleMethodProvider(element: Element, named: String?, typeElement: Element, target: TargetType, implementations: MutableList<DependencyProvider>) {
        for (provider in methodsWithDependencyAnnotation()) {
            val isKotlinModule = isModuleKotlinObject(provider.enclosingElement.asTypeElement())
            // if method is abstract skip
            if (provider.isAbstract()) continue

            if (!provider.isStatic() && !isKotlinModule) {
                throw ProcessorException("${provider.enclosingElement.simpleName}.${provider.simpleName}() is annotated with @Dependency must be static and public").setElement(provider)
            }

            if (!provider.returnType.isEqualTo(element) || !qualifierFinder.hasNamed(named, provider)) continue

            val returnType = provider.returnType.asTypeElement()

            val type = createModuleMethodProvider(returnType, typeElement, target, provider.parameters, isMethod = true)
            type.isKotlinModule = isKotlinModule
            type.name = provider.simpleName
            type.module = ClassName.get(provider.enclosingElement.asTypeElement())
            type.isSingleton = type.isSingleton || provider.isSingleton()
            if (type.isSingleton) {
                type.packageName = returnType.getPackage().toString()
                if (type.dependencyModels.any { target.isSubtype(it.dependency, it.originalType) }) {
                    throw ProcessorException("target can't be user as dependency in Singleton").setElement(provider)
                }
            }
            implementations.add(type)
        }
    }

    private fun createModuleMethodProvider(
        implementation: TypeElement,
        parent: Element,
        target: TargetType,
        methodParameters: List<Element>,
        isMethod: Boolean): DependencyProvider {

        val modulePackageName = implementation.getPackage().toString()

        val named = qualifierFinder.getQualifier(implementation)
        val provider = DependencyProvider(implementation, implementation.isSingleton(), ClassName.get(implementation))
        provider.named = named
        provider.isMethod = isMethod
        if (provider.isSingleton) {
            provider.packageName = modulePackageName
        }

        if (methodParameters.isEmpty()) return provider
        for (dependency in methodParameters) {
            if (target.isLocalScope(dependency, dependency)) {
                provider.dependencyModels.add(targetDependencyModel(dependency))
                continue
            }

            if (target.isSubtype(dependency, dependency)) {
                provider.dependencyModels.add(targetDependencyModel(dependency))
                continue
            }

            if (dependency.isEqualTo(parent.asType())) {
                throw ProcessorException("Cyclic graph detected building ${dependency.asType()} cyclic: ${dependency.asType()}").setElement(parent)
            }
            val resolved = dependencyResolver.resolveDependency(dependency, target = target)
            provider.dependencyModels.add(resolved)
        }

        return provider
    }


    fun collectSuperTypes(typeElement: TypeElement?, returnTypes: MutableSet<TypeMirror>) {
        typeElement ?: return

        // first check and collect super interfaces recursively
        for (typeInterface in typeElement.interfaces) {
            returnTypes.add(typeInterface)
            collectSuperTypes(typeInterface.asTypeElement(), returnTypes)
        }
        // if super class is Object or is not present return
        if (typeElement.superclass.isNotValid()) return
        // else put superclass
        returnTypes.add(typeElement.superclass)
        // collect all superclass's superclasses
        val superclass = typeElement.superclass ?: return
        if (superclass.kind != TypeKind.NONE) {
            collectSuperTypes(superclass.asTypeElement(), returnTypes)
        }
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