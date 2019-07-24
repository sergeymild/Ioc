package com.ioc

import com.ioc.common.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
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
        scoped: String? = null,
        skipCheckFromTarget: Boolean = false,
        parents: ParensSet): DependencyModel {

        var setterMethod: ExecutableElement? = null
        var dependencyElement = element
        val fieldName = dependencyElement.simpleName.toString()

        if (element.isMethod() && element.isPrivate()) {
            throw ProcessorException("@Inject annotation is placed on method `$element` in `${element.enclosingElement}` with private access").setElement(element)
        }

        // If @Inject annotation is placed on private field
        // try to find setter method with one parameter and same type
        if (element.isPrivate()) {
            setterMethod = findDependencySetter(element).orElse {
                throw ProcessorException("@Inject annotation placed on field `${element.simpleName}` in `${element.enclosingElement.simpleName}` with private access and which does't have public setter method.").setElement(element)
            }

            if (setterMethod.isPrivate()) {
                throw ProcessorException("@Inject annotation is placed on field `$element` in `${element.enclosingElement.simpleName}` with private access").setElement(element)
            }
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

        val isInterface = dependencyTypeElement.kind == ElementKind.INTERFACE
        val isSingleton = dependencyElement.isHasAnnotation(Singleton::class.java)
            || dependencyTypeElement.isHasAnnotation(Singleton::class.java)


        val named = named
            ?: qualifierFinder.getQualifier(element)
            ?: qualifierFinder.getQualifier(dependencyElement)
            ?: qualifierFinder.getQualifier(dependencyTypeElement)
        //?: ""

        val scoped = scoped
            ?: ScopeFinder.getScope(element)
            ?: ScopeFinder.getScope(dependencyElement)
            ?: ScopeFinder.getScope(dependencyTypeElement)
            ?: ROOT_SCOPE

        if ((ScopeFinder.getScope(dependencyTypeElement)
                ?: ROOT_SCOPE) != ROOT_SCOPE && isInterface) {
            throw ProcessorException("@$scoped must be placed on one of the implementations `$dependencyTypeElement`").setElement(dependencyTypeElement)
        }

        var isTarget = TargetChecker.isSubtype(target, dependencyElement, scoped)
        if (!isTarget && setterMethod != null) {
            isTarget = TargetChecker.isSubtype(target, dependencyElement, scoped)
        }

        val order = dependencyElement.getAnnotation(InjectPriority::class.java)?.value
            ?: Int.MAX_VALUE

        val dependencyImplementations = if (isTarget) emptyList() else dependencyTypesFinder.findFor(dependencyElement, named, target, dependencyTypeElement, isSingleton || skipCheckFromTarget, parents)
        // if we did't find any providers of this type, try to find constructors of concrete type
        var argumentConstructor = if (dependencyImplementations.isEmpty()) findArgumentConstructor(dependencyTypeElement) else null
        val noArgsConstructor = if (dependencyImplementations.isEmpty()) findEmptyConstructor(dependencyTypeElement) else null

        val dependencies = IProcessor.singletons.getOrDefault("${dependencyTypeElement.asType()}", mutableListOf())

        if (dependencies.isEmpty()) {
            argumentConstructor?.let {
                parents.add(dependencyTypeElement)
                resolveConstructorArguments(dependencyTypeElement, it, dependencies, target, dependencyTypeElement, isSingleton, parents)
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
        depdendency.depencencies = dependencies
        depdendency.order = order
        depdendency.named = named
        // Assign correct scope
        // first check if we have root scope
        // and if it is, check on implementation
        // if implementation have root scope or null
        // check on dependency
        if (depdendency.scoped == ROOT_SCOPE) {
            val implementationScope = depdendency.implementations.firstOrNull()?.scoped
            if (implementationScope == null || implementationScope == ROOT_SCOPE) {
                //depdendency.scoped = ScopeFinder.getScope(depdendency.dependency)
                depdendency.scoped = scoped
            } else {
                depdendency.scoped = implementationScope
            }
        }
        depdendency.isViewModel = isViewModel

        if (!skipCheckFromTarget) {
            if (dependencyImplementations.none { it.isFromTarget } && argumentConstructor == null) {
                TargetChecker.isFromTarget(target, dependencyTypeElement.asType())?.let {
                    // if we have in target field with @Inject annotation
                    // which means we must pass this dependency instead create new one
                    val fields = target.fields + target.supertypes.map { it.asTypeElement() }.flatMap { it.fields() }
                    val field = fields.firstOrNull { it.isHasAnnotation(Inject::class.java) && it.isEqualTo(depdendency.dependency) }
                    // prefer argument constructor
                    argumentConstructor = depdendency.implementations.map { findArgumentConstructor(it.returnType()) }.firstOrNull()
                        ?: argumentConstructor
                    depdendency.isFromTarget = (argumentConstructor == null || field != null) && !isSingleton
                    depdendency.isLocal = true
                    depdendency.targetMethod = it
                }
            }
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

    private fun resolveConstructorArguments(typeElement: TypeElement,
                                            argumentConstructor: ExecutableElement?,
                                            dependencies: MutableList<DependencyModel>,
                                            target: TargetType,
                                            parent: Element,
                                            isParentSingleton: Boolean,
                                            parents: ParensSet) {

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
            val scoped = ScopeFinder.getScope(argument)
                ?: ScopeFinder.getScope(argument.asTypeElement())
                ?: ROOT_SCOPE
            val isTarget = TargetChecker.isSubtype(target, element, scoped)

            if (isTarget && !isParentSingleton) {
                val dependency = DependencyModel(target.element, target.element, element.simpleName.toString(), types.erasure(target.element.asType()), false, false, false)
                dependency.name = "target"
                dependency.asTarget = true
                dependency.scoped = scoped
                dependencies.add(dependency)
                continue
            }

            //val implementations = dependencyTypesFinder.findFor(element.asTypeElement(), named, target, parent, isParentSingleton)

            //val dependency = implementations.firstOrNull()?.transform { it.method } ?: element
            var newTarget = target
            if (isParentSingleton) {
                newTarget = IProcessor.createTarget(typeElement, dependencyTypesFinder)
            }
            parents.add(typeElement)
            // TODO зачем это делать, если мы нашли implementations
            resolveDependency(element, newTarget, named, scoped, isParentSingleton, parents).let {
                //                if (!it.asTarget) {
//                    it.asTarget = implementations.flatMap { it.dependencyModels }.any { it.asTarget }
//                }
                it.isWeakDependency = isWeakDependency
                it.isProvider = isProvider
                it.isLazy = isLazy
                if (it.asTarget && isParentSingleton) {
                    it.asTarget = false
                    it.name = element.simpleName.toString()
                }
                // prefer method with @Dependency over target method
//                if (it.isFromTarget && implementations.any { it.isMethod && !it.isFromTarget }) {
//                    it.isFromTarget = false
//                }
                if (it.named.isNullOrEmpty()) {
                    it.named = qualifierFinder.getQualifier(argument)
                }

                if (it.scoped == ROOT_SCOPE) {
                    it.scoped = if (scoped != ROOT_SCOPE || it.implementations.isEmpty()) scoped else it.implementations[0].scoped
                }

                //it.implementations = implementations
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

    @Throws(ProcessorException::class)
    private fun findDependencySetter(element: Element): ExecutableElement? {
        return element.enclosingElement.methods {
            it.isPublic() && it.parameters.size == 1 && it.parameters[0].isEqualTo(element)
        }.firstOrNull()
    }
}