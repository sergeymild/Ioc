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
        named: String? = null,
        skipCheckFromTarget: Boolean = false): DependencyModel {

        var setterMethod: ExecutableElement? = null
        var dependencyElement = element
        val fieldName = dependencyElement.simpleName.toString()
        var getterMethod = fieldName

        val isLocalScope = element.isHasAnnotation(LocalScope::class.java)

        if (element.isMethod() && element.isPrivate()) {
            throw ProcessorException("@Inject annotation is placed on method `$element` in `${element.enclosingElement}` with private access").setElement(element)
        }

        // If @Inject annotation is placed on private field
        // try to find setter method with one parameter and same type
        if (element.isPrivate()) {
            val result = findSetterAndGetterMethods(element)
            setterMethod = result.setter
            getterMethod = result.getter.toGetterName()
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

        // Check field
        val isViewModel = dependencyElement.isViewModel()

        if (isViewModel && !target.element.isCanHaveViewModel()) {
            throw ProcessorException("@Inject annotation is placed on `${dependencyElement.asType()}` class which declared not in either FragmentActivity or Fragment").setElement(element)
        }


        val isProvider = dependencyElement.isProvideDependency(types)
        if (isProvider) dependencyElement = element.getGenericFirstType()
        val isWeakDependency = dependencyElement.isWeakDependency(types)
        if (isWeakDependency) dependencyElement = dependencyElement.getGenericFirstType()
        val isLazy = dependencyElement.isLazy(types)
        if (isLazy) dependencyElement = dependencyElement.getGenericFirstType()

        val dependencyTypeElement = dependencyElement.asTypeElement()


        val isSingleton = dependencyElement.isHasAnnotation(Singleton::class.java)
            || dependencyTypeElement.isHasAnnotation(Singleton::class.java)


        val named = named
            ?: qualifierFinder.getQualifier(element)
            ?: qualifierFinder.getQualifier(dependencyElement)
            ?: qualifierFinder.getQualifier(dependencyTypeElement)

        var isTarget = TargetChecker.isSubtype(target, dependencyElement)
        if (!isTarget && setterMethod != null) {
            isTarget = TargetChecker.isSubtype(target, dependencyElement)
        }

        val order = dependencyElement.getAnnotation(InjectPriority::class.java)?.value
            ?: Int.MAX_VALUE

        val dependencyImplementations = if (isTarget) emptyList() else dependencyTypesFinder.findFor(dependencyElement, named, target, dependencyTypeElement, isSingleton || skipCheckFromTarget)
        // if we did't find any providers of this type, try to find constructors of concrete type
        var argumentConstructor = if (dependencyImplementations.isEmpty()) findArgumentConstructor(dependencyTypeElement) else null
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
        depdendency.order = order
        depdendency.named = named

        depdendency.isViewModel = isViewModel


        if (isLocalScope) {
            target.localScopeDependencies[element.asType().toString()] = getterMethod
//            if (dependencyImplementations.none { it.isFromTarget } && argumentConstructor == null) {
//                TargetChecker.isFromTarget(target, dependencyTypeElement.asType())?.let {
//                    // if we have in target field with @Inject annotation
//                    // which means we must pass this dependency instead create new one
//                    val fields = target.fields + target.supertypes.map { it.asTypeElement() }.flatMap { it.fields() }
//                    val field = fields.firstOrNull { it.isHasAnnotation(Inject::class.java) && it.isEqualTo(depdendency.dependency) }
//                    // prefer argument constructor
//                    argumentConstructor = depdendency.implementations.map { findArgumentConstructor(it.returnType()) }.firstOrNull()
//                        ?: argumentConstructor
//                    depdendency.isFromTarget = (argumentConstructor == null || field != null) && !isSingleton
//                    depdendency.isLocal = true
//                    depdendency.targetMethod = it
//                }
//            }
        }
        depdendency.asTarget = isTarget
        resolveDependencyName(depdendency, isSingleton)
        depdendency.setterMethod = setterMethod
        depdendency.argumentsConstructor = argumentConstructor
        depdendency.emptyConstructor = noArgsConstructor

        return depdendency
    }

    private fun resolveDependencyName(dependency: DependencyModel, isSingleton: Boolean) {
        if (dependency.asTarget) dependency.name = "target"
        dependency.isSingleton = isSingleton || dependency.implementations.any { it.isSingleton }
        if (dependency.isSingleton) {
            if (dependency.implementations.isNotEmpty()) {
                dependency.generatedName = "singleton_${dependency.implementations[0].name.decapitalize()}"
            } else {
                dependency.generatedName = "singleton_${dependency.name}"
            }
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

//        val typeElementSupertypes = mutableListOf<TypeMirror>()
//        dependencyTypesFinder.collectSuperTypes(typeElement, typeElementSupertypes)

        // TODO generic type
        for (argument in constructorArguments) {
            // Ioc not supported primitive types for now
            if (argument.asType().kind.isPrimitive) return
            var element: Element = argument
            val isWeakDependency = argument.isWeakDependency(types)
            if (isWeakDependency) element = argument.getGenericFirstType()

            val isProvider = argument.isProvideDependency(types)
            if (isProvider) element = argument.getGenericFirstType()

            val isLazy = argument.isLazy(types)
            if (isLazy) element = argument.getGenericFirstType()

            val named = qualifierFinder.getQualifier(argument)

            val isTarget = TargetChecker.isSubtype(target, element)

            if (isTarget && !isParentSingleton) {
                val dependency = DependencyModel(target.element, target.element, element.simpleName.toString(), types.erasure(target.element.asType()), false, false, false)
                dependency.name = "target"
                dependency.asTarget = true
                dependencies.add(dependency)
                continue
            }

            var newTarget = target
            if (isParentSingleton) {
                newTarget = IProcessor.createTarget(typeElement, dependencyTypesFinder)
            }
            // TODO зачем это делать, если мы нашли implementations
            resolveDependency(element, newTarget, named, isParentSingleton).let {
                it.isWeakDependency = isWeakDependency
                it.isProvider = isProvider
                it.isLazy = isLazy
                if (it.asTarget && isParentSingleton) {
                    it.asTarget = false
                    it.name = element.simpleName.toString()
                }

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