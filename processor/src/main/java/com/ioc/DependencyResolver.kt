package com.ioc

import com.ioc.IProcessor.Companion.projectSingletons
import com.ioc.common.*
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

class DependencyResolver(
    private val qualifierFinder: QualifierFinder,
    private val dependencyTypesFinder: DependencyTypesFinder) {

    var cachedConstructorArguments = mutableMapOf<String, List<DependencyModel>>()

    @Throws(ProcessorException::class)
    fun resolveDependency(
        element: Element,
        target: TargetType,
        named: String? = null): DependencyModel {

        //val isTarget = TargetChecker.isSubtype(target, element)
        if (target.isSubtype(element, element)) return targetDependencyModel(element)

        var setterMethod: ExecutableElement? = null
        var dependencyElement = element
        val fieldName = dependencyElement.simpleName

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

        if (isViewModel) validateIsAllowCanHaveViewModel(dependencyElement, target.element)


        val isProvider = dependencyElement.isProvider()
        val isWeak = dependencyElement.isWeak()
        val isLazy = dependencyElement.isLazy()
        if (isProvider || isWeak || isLazy) {
            dependencyElement = dependencyElement.getGenericFirstType()
        }

        var dependencyTypeElement = dependencyElement.asTypeElement()

        if (target.isSubtype(element, dependencyTypeElement))
            return targetDependencyModel(element)


        var isSingleton = dependencyTypeElement.isSingleton()


        val named = named
            ?: qualifierFinder.getQualifier(element)
            ?: qualifierFinder.getQualifier(dependencyElement)
            ?: qualifierFinder.getQualifier(dependencyTypeElement)

        var possibleImplementation: DependencyModel? = null
        if (dependencyTypeElement.isInterface() || dependencyTypeElement.isAbstract()) {
            possibleImplementation = dependencyTypesFinder.findImplementationFromAbstractModuleMethodProviderOrFromClassWithDependencyAnnotation(dependencyElement, named)
        }
        possibleImplementation?.let {
            dependencyTypeElement = it.originalType.asTypeElement()
            isSingleton = it.isSingleton || isSingleton
        }

        // return cached singleton
        projectSingletons[dependencyTypeElement.asTypeString()]?.let {
            return it.copy().also { m ->
                m.fieldName = fieldName
                m.setterMethod = setterMethod
            }
        }

        var methodProvider: ModuleMethodProvider? = null
        if (possibleImplementation == null) {
            methodProvider = dependencyTypesFinder.findFor(dependencyElement, named, target, dependencyTypeElement)
        }

        // if we did't find any providers of this type, try to find constructors of concrete type
        val dependencyConstructor = if (methodProvider == null) findArgumentConstructor(dependencyTypeElement) else null

        var dependencies = cachedConstructorArguments.getOrDefault(dependencyTypeElement.asTypeString(), emptyList())
            .map { it.copy() }

        if (dependencies.isEmpty()) {
            dependencyConstructor?.let {
                dependencies = resolveConstructorArguments(dependencyTypeElement, it, target, isSingleton)
                cachedConstructorArguments[dependencyTypeElement.asTypeString()] = dependencies
            }
        }

        val dependency = DependencyModel(
            dependency = dependencyElement,
            originalType = dependencyTypeElement,
            fieldName = fieldName,
            isProvider = isProvider,
            isLazy = isLazy,
            isWeak = isWeak,
            isSingleton = methodProvider?.isSingleton ?: isSingleton,
            isViewModel = isViewModel)


        dependency.methodProvider = methodProvider
        dependency.typeArguments.addAll(if (isProvider || isWeak || isLazy) emptyList() else element.asType().typeArguments())
        dependency.dependencies = dependencies
        dependency.named = named

        dependency.setterMethod = setterMethod
        dependency.constructor = dependencyConstructor

        if (dependency.isSingleton) {
            projectSingletons[dependency.originalTypeString] = dependency
        }
        return dependency
    }

    private fun resolveConstructorArguments(
        typeElement: TypeElement,
        argumentConstructor: ExecutableElement?,
        target: TargetType,
        isParentSingleton: Boolean): List<DependencyModel> {
        val constructorDependencies = mutableListOf<DependencyModel>()
        val constructorArguments = argumentConstructor?.parameters ?: emptyList()

        var newTarget = target
        if (isParentSingleton) {
            newTarget = IProcessor.createTarget(typeElement, dependencyTypesFinder)
        }

        // TODO generic type
        for (argument in constructorArguments) {
            if (target.isLocalScope(argument, argument) && !isParentSingleton) {
                constructorDependencies.add(targetDependencyModel(argument))
                continue
            }

            // Ioc not supported primitive types for now
            if (argument.isPrimitive()) {
                throw ProcessorException("Constructor used primitive type").setElement(argument)
            }

            val dependency = resolveDependency(argument, newTarget)
            constructorDependencies.add(dependency)
        }
        return constructorDependencies
    }

    private fun findArgumentConstructor(typeElement: TypeElement): ExecutableElement? {
        // first check if we have constructor with @Inject annotation
        val constructors = ElementFilter.constructorsIn(typeElement.enclosedElements)
        var constructor = constructors.firstOrNull { it.isHasAnnotation(Inject::class.java) }

        if (constructor != null && constructor.isPrivate()) {
            throw ProcessorException("${constructor.enclosingElement}.${constructor.simpleName} contains @Inject must be public")
        }

        if (constructor == null && constructors.size == 1) constructor = constructors.firstOrNull()

        if (constructor != null && constructor.parameters.any { !it.isSupportedType() }) {
            throw ProcessorException("@Inject annotation placed on constructor in ${constructor.enclosingElement} which have unsupported parameters.").setElement(constructor)
        }

        if (constructor == null) constructor = constructors.firstOrNull { it.parameters.isEmpty() }
        if (constructor != null && constructor.isPrivate()) {
            throw ProcessorException("Cant find suitable constructors ${constructor.enclosingElement}").setElement(constructor)
        }

        return constructor
    }
}