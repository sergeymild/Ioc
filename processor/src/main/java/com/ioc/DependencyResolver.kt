package com.ioc

import com.ioc.common.*
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
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


        var isProvider = dependencyElement.isProvider()
        var isWeak = dependencyElement.isWeak()
        var isLazy = dependencyElement.isLazy()
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
            isProvider = it.isProvider || isProvider
            isLazy = it.isLazy || isLazy
            isWeak = it.isWeak || isWeak
            isSingleton = it.isSingleton || isSingleton
        }

        var methodProvider: ModuleMethodProvider? = null
        if (possibleImplementation == null) {
            methodProvider = dependencyTypesFinder.findFor(dependencyElement, named, target, dependencyTypeElement)
        }

        // if we did't find any providers of this type, try to find constructors of concrete type
        var argumentConstructor = if (methodProvider == null) findArgumentConstructor(dependencyTypeElement) else null
        var noArgsConstructor = if (methodProvider == null) findEmptyConstructor(dependencyTypeElement) else null

        if (argumentConstructor != null && argumentConstructor.parameters.isEmpty() && noArgsConstructor == null) {
            noArgsConstructor = argumentConstructor
            argumentConstructor = null
        }

        var dependencies = IProcessor.singletons.getOrDefault("${dependencyTypeElement.asType()}", emptyList()).map { it.copy() }

        if (dependencies.isEmpty()) {
            argumentConstructor?.let {
                dependencies = resolveConstructorArguments(dependencyTypeElement, it, target, isSingleton)
                IProcessor.singletons["${dependencyTypeElement.asType()}"] = dependencies
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
        dependency.dependencies = dependencies.map { it.copy() }
        dependency.named = named

        //if (!dependency.isSingleton) uniqueName(dependency)
        dependency.setterMethod = setterMethod
        dependency.argumentsConstructor = argumentConstructor
        dependency.emptyConstructor = noArgsConstructor

        return dependency
    }

    private fun resolveConstructorArguments(
        typeElement: TypeElement,
        argumentConstructor: ExecutableElement?,
        target: TargetType,
        isParentSingleton: Boolean): List<DependencyModel> {
        val constructorDependencies = mutableListOf<DependencyModel>()
        val constructorArguments = argumentConstructor?.parameters ?: emptyList()

        // TODO generic type
        for (argument in constructorArguments) {
            // TODO !isParentSingleton
            if (target.isSubtype(argument, argument) && !isParentSingleton) {
                constructorDependencies.add(targetDependencyModel(argument))
                continue
            }

            if (target.isLocalScope(argument, argument) && !isParentSingleton) {
                constructorDependencies.add(targetDependencyModel(argument))
                continue
            }

            // Ioc not supported primitive types for now
            if (argument.isPrimitive()) {
                throw ProcessorException("Constructor used primitive type").setElement(argument)
            }
            var element: Element = argument
            val isProvider = element.isProvider()
            val isWeakDependency = element.isWeak()
            val isLazy = element.isLazy()
            if (isProvider || isWeakDependency || isLazy) {
                element = element.getGenericFirstType()
            }

            val named = qualifierFinder.getQualifier(argument)

            var newTarget = target
            if (isParentSingleton) {
                newTarget = IProcessor.createTarget(typeElement, dependencyTypesFinder)
            }

            val dependency = resolveDependency(element, newTarget, named)//.let {
                dependency.isWeak = isWeakDependency
                dependency.isProvider = isProvider
                dependency.isLazy = isLazy
                if (dependency.named.isNullOrEmpty()) {
                    dependency.named = qualifierFinder.getQualifier(argument)
                }

                //it.originalType = element

                constructorDependencies.add(dependency)
            //}
        }
        return constructorDependencies
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
            .firstOrNull { it.parameters.isEmpty() && !it.isHasAnnotation(Inject::class.java) && !it.modifiers.contains(Modifier.PRIVATE) }
            ?.let { return it }
        return null
    }
}