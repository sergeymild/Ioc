package com.ioc

import com.ioc.common.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

class DependencyResolver(
    private val types: Types,
    private val qualifierFinder: QualifierFinder,
    private val dependencyTypesFinder: DependencyTypesFinder) {

    @Throws(ProcessorException::class)
    fun resolveDependency(
        element: Element,
        target: TargetType,
        named: String? = null): DependencyModel {

        //val isTarget = TargetChecker.isSubtype(target, element)
        if (target.isSubtype(element))
            return targetDependencyModel(element)

        var setterMethod: ExecutableElement? = null
        var dependencyElement = element
        val fieldName = dependencyElement.simpleName.toString()

        if (element.isMethod() && element.isPrivate()) {
            throw ProcessorException("@Inject annotation is placed on method `$element` in `${element.enclosingElement}` with private access").setElement(element)
        }

        // If @Inject annotation is placed on private field
        // try to find setter method with one parameter and same type
        if (element.isPrivate()) {
            val result = findSetterAndGetterMethods(element)
            setterMethod = result.setter
        }

        // If @Inject annotation is placed on setter method
        if (element.isMethod()) {
            setterMethod = element as ExecutableElement
            if (setterMethod.parameters.size > 1) {
                throw ProcessorException("@Inject annotation is placed on method `$element` in `${element.enclosingElement.simpleName}` with more than one parameter").setElement(element)
            }

            if (setterMethod.isPrivate()) {
                throw ProcessorException("@Inject annotation is placed on method `$element` in `${element.enclosingElement.simpleName}` with private access").setElement(element)
            }

            dependencyElement = setterMethod.parameters[0]
        }

        // Check is android viewModel field
        val isViewModel = dependencyElement.isViewModel()

        if (isViewModel && !target.element.isCanHaveViewModel()) {
            throw ProcessorException("@Inject annotation is placed on `${dependencyElement.asType()}` class which declared not in either FragmentActivity or Fragment").setElement(element)
        }


        val isProvider = dependencyElement.isProvideDependency()
        val isWeakDependency = dependencyElement.isWeakDependency()
        val isLazy = dependencyElement.isLazyDependency()
        if (isProvider || isWeakDependency || isLazy) {
            dependencyElement = dependencyElement.getGenericFirstType()
        }

        val dependencyTypeElement = dependencyElement.asTypeElement()


        val isSingleton = dependencyTypeElement.isHasAnnotation(Singleton::class.java)


        val named = named
            ?: qualifierFinder.getQualifier(element)
            ?: qualifierFinder.getQualifier(dependencyElement)
            ?: qualifierFinder.getQualifier(dependencyTypeElement)

        val dependencyImplementations = dependencyTypesFinder.findFor(dependencyElement, named, target, dependencyTypeElement)

        // if we did't find any providers of this type, try to find constructors of concrete type
        val argumentConstructor = if (dependencyImplementations.isEmpty()) findArgumentConstructor(dependencyTypeElement) else null
        val noArgsConstructor = if (dependencyImplementations.isEmpty()) findEmptyConstructor(dependencyTypeElement) else null

        val dependencies = IProcessor.singletons.getOrDefault("${dependencyTypeElement.asType()}", mutableListOf())

        if (dependencies.isEmpty()) {
            argumentConstructor?.let {
                resolveConstructorArguments(dependencyTypeElement, it, dependencies, target, isSingleton)
                IProcessor.singletons["${dependencyTypeElement.asType()}"] = dependencies
            }
        }

        val depdendency = DependencyModel(dependencyElement,
            dependencyElement,
            fieldName,
            types.erasure(dependencyElement.asType()),
            isProvider,
            isLazy,
            isWeakDependency)


        depdendency.implementations = dependencyImplementations
        depdendency.typeArguments.addAll(if (isProvider || isWeakDependency || isLazy) emptyList() else element.asType().typeArguments())

        depdendency.originalType = dependencyTypeElement
        depdendency.dependency = depdendency.implementations.firstOrNull()?.method
            ?: dependencyTypeElement
        depdendency.dependencies = dependencies
        depdendency.named = named

        depdendency.isViewModel = isViewModel

        resolveDependencyName(depdendency, isSingleton)
        depdendency.setterMethod = setterMethod
        depdendency.argumentsConstructor = argumentConstructor
        depdendency.emptyConstructor = noArgsConstructor

        return depdendency
    }

    private fun resolveDependencyName(dependency: DependencyModel, isSingleton: Boolean) {
        dependency.isSingleton = isSingleton || dependency.implementations.any { it.isSingleton }
        if (dependency.isSingleton) {
            dependency.generatedName = dependency.simpleName
        } else {
            dependency.generatedName = uniqueName(dependency.name).decapitalize()
        }
    }

    private fun resolveConstructorArguments(
        typeElement: TypeElement,
        argumentConstructor: ExecutableElement?,
        dependencies: MutableList<DependencyModel>,
        target: TargetType,
        isParentSingleton: Boolean) {

        val constructorArguments = argumentConstructor?.parameters ?: emptyList()

        // TODO generic type
        for (argument in constructorArguments) {
            // TODO !isParentSingleton
            if (target.isSubtype(argument) && !isParentSingleton) {
                dependencies.add(targetDependencyModel(argument))
                continue
            }

            // Ioc not supported primitive types for now
            if (argument.asType().kind.isPrimitive) return
            var element: Element = argument
            val isProvider = element.isProvideDependency()
            val isWeakDependency = element.isWeakDependency()
            val isLazy = element.isLazyDependency()
            if (isProvider || isWeakDependency || isLazy) {
                element = element.getGenericFirstType()
            }

            val named = qualifierFinder.getQualifier(argument)

            var newTarget = target
            if (isParentSingleton) {
                newTarget = IProcessor.createTarget(typeElement, dependencyTypesFinder)
            }

            resolveDependency(element, newTarget, named).let {
                it.isWeakDependency = isWeakDependency
                it.isProvider = isProvider
                it.isLazy = isLazy

                if (it.named.isNullOrEmpty()) {
                    it.named = qualifierFinder.getQualifier(argument)
                }

                it.originalType = element

                dependencies.add(it)
            }
        }
    }

    private fun findArgumentConstructor(typeElement: TypeElement): ExecutableElement? {
        // first check if we have constructor with @Inject annotation
        val constructors = ElementFilter.constructorsIn(typeElement.enclosedElements)
        var constructor = constructors.firstOrNull { it.isHasAnnotation(Inject::class.java) }

        // then check maybe we have only one constructor with arguments
        if (constructor == null && constructors.size == 1 && constructors[0].parameters.isNotEmpty()) {
            constructor = constructors[0]
        }

        if (constructor != null && constructor.parameters.any { !it.isSupportedType() }) {
            throw ProcessorException("@Inject annotation placed on constructor in ${constructor.enclosingElement} which have unsupported parameters.").setElement(constructor)
        }

        // else return null
        return constructor
    }

    private fun findEmptyConstructor(typeElement: TypeElement): ExecutableElement? {
        ElementFilter.constructorsIn(typeElement.enclosedElements)
            .firstOrNull { it.parameters.isEmpty() && !it.isHasAnnotation(Inject::class.java) }
            ?.let { return it }
        return null
    }
}