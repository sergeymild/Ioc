package com.ioc

import com.ioc.IProcessor.Companion.projectSingletons
import com.ioc.common.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

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
            throwsInjectPlacedOnPrivateMethod(element)
        }

        // If @Inject annotation is placed on private field
        // try to find setter method with one parameter and same type
        if (element.isPrivate()) {
            validateContainsSetterAndGetterInParent(element)
            setterMethod = findDependencySetter(element).orElse { throwsSetterIsNotFound(element) }
        }

        // If @Inject annotation is placed on setter method
        if (element.isMethod()) {
            setterMethod = element.asMethod()
            validateSetterMethod(setterMethod)
            dependencyElement = setterMethod.parameters[0]
        }

        // Check is android viewModel field
        val isViewModel = dependencyElement.isAndroidViewModel()

        if (isViewModel) validateIsAllowCanHaveViewModel(dependencyElement, target.element)


        val isProvider = dependencyElement.isProvider()
        val isWeak = dependencyElement.isWeak()
        val isLazy = dependencyElement.isLazy()
        if (isProvider || isWeak || isLazy) {
            dependencyElement = dependencyElement.getGenericFirstType().asElement()
        }

        var dependencyTypeElement = dependencyElement.asTypeElement()

        if (target.isSubtype(element, dependencyTypeElement))
            return targetDependencyModel(element)


        var isSingleton = dependencyTypeElement.isSingleton()
        var isLocalScoped = element.isLocalScoped()
        isLocalScoped = dependencyElement.isLocalScoped() || isLocalScoped
        isLocalScoped = dependencyTypeElement.isLocalScoped() || isLocalScoped

        val named = named
            ?: qualifierFinder.getQualifier(element)
            ?: qualifierFinder.getQualifier(dependencyElement)
            ?: qualifierFinder.getQualifier(dependencyTypeElement)

        val possibleImplementation: DependencyModel? =
            dependencyTypesFinder.findImplementationFromAbstractModuleMethodProviderOrFromClassWithDependencyAnnotation(dependencyElement, named)

        possibleImplementation?.let {
            dependencyTypeElement = it.originalType.asTypeElement()
            isSingleton = it.isSingleton || isSingleton
            isLocalScoped = it.isLocal || isLocalScoped
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
            isViewModel = isViewModel,
            isLocal = methodProvider?.isLocal ?: isLocalScoped)


        dependency.methodProvider = methodProvider
        dependency.typeArguments.addAll(if (isProvider || isWeak || isLazy) emptyList() else element.asType().typeArguments())
        dependency.dependencies = methodProvider?.dependencies ?: dependencies
        dependency.named = named

        dependency.setterMethod = setterMethod
        dependency.constructor = dependencyConstructor

        if (dependency.isSingleton) {
            if (dependency.dependency.isGeneric()) {
                throw ProcessorException("Singleton with generic types doesn't support").setElement(dependency.dependency)
            }
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
            newTarget = createTarget(typeElement, dependencyTypesFinder)
        }

        // TODO generic type
        for (argument in constructorArguments) {
            if (target.isLocalScope(argument, argument) && !isParentSingleton) {
                constructorDependencies.add(targetDependencyModel(argument))
                continue
            }

            // Ioc not supported primitive types for now
            validateConstructorParameter(argument)

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
            throwsConstructorIsPrivate(constructor)
        }

        if (constructor == null && constructors.size == 1) constructor = constructors.firstOrNull()

        if (constructor != null && constructor.parameters.any { !it.isSupportedType() }) {
            throwsConstructorHasUnsupportedParameters(constructor)
        }

        if (constructor == null) constructor = constructors.firstOrNull { it.parameters.isEmpty() }
        if (constructor != null && constructor.isPrivate()) throwsDidNotFindSuitableConstructor(constructor)

        return constructor
    }
}