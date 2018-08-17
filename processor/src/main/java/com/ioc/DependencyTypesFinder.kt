package com.ioc

import com.ioc.IProcessor.Companion.qualifierFinder
import com.ioc.common.*
import com.squareup.javapoet.ClassName
import javax.annotation.processing.RoundEnvironment
import javax.inject.Singleton
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

val excludedPackages = setOf("java", "sun", "org.jetbrains", "android.content", "android.util")

class DependencyTypesFinder(private val roundEnv: RoundEnvironment,
                            private val qualifierFinder: QualifierFinder) {

    lateinit var dependencyResolver: DependencyResolver

    @Throws(ProcessorException::class)
    fun findFor(element: Element,
                named: String?,
                target: TargetType,
                typeElement: Element,
                skipCheckFromTarget: Boolean = false,
                parents: ParensSet) : List<DependencyProvider> {

        val isInterface = typeElement.kind == ElementKind.INTERFACE
        val isAbstractClass = typeElement.modifiers.contains(Modifier.ABSTRACT)

        val implementations = mutableListOf<DependencyProvider>()


        if (!skipCheckFromTarget) {
            // try check maybe target has dependency implementation method
            TargetChecker.isFromTarget(target, element.asType())?.let {
                val returnType = it.returnType.asDeclared().asTypeElement()
                val type = DependencyProvider(returnType, false, returnType.asClassName())
                type.methodType = it
                type.isMethod = true
                type.isLocal = true
                type.name = it.simpleName.toString()
                type.isFromTarget = true
                implementations.add(type)
                parents.add(target.element)
                return implementations
            }
        }

        // Try find dependency by annotated @Dependency method
        findMethodProviders(element, named, parents, typeElement, target, implementations)

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@"+named}] founded [${implementations.joinToString { it.method.asType().toString() }}]")
                    .setElement(element)
        }

        if (implementations.isNotEmpty()) {
            parents.add(implementations[0].returnType())
            return implementations
        }

        if (!isInterface && !isAbstractClass) return emptyList()

        // prefer arguments constructor
        if (!isInterface && !isAbstractClass && element.isHasArgumentsConstructor()) {
            implementations.add(createProvider(element.asTypeElement(), typeElement, target, parents = parents))
            parents.add(element)
            return implementations
        }

        if (!isInterface && !isAbstractClass) {
            implementations.add(createProvider(element.asTypeElement(), typeElement, target, parents = parents))
            parents.add(element)
            return implementations
        }

        // try to find in interface's abstract methods
        for (method in abstractMethodsWithDependencyAnnotations()) {
            // method must contain only one parameter and must be interface
            val implementation = method.parameters[0].asTypeElement()
            val `interface` = method.returnType.asTypeElement()

            if (isSubtype(element, `interface`, named)) {
                implementations.add(createProvider(implementation, typeElement, target, parents = parents))
            }
        }

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@"+named}] founded [${implementations.joinToString { it.method.asType().toString() }}]")
                    .setElement(element)
        }

        if (implementations.isNotEmpty()) {
            parents.add(implementations[0].returnType())
            return implementations
        }

        for (provider in classesWithDependencyAnnotation()) {
            if (isInterface && element.isEqualTo(provider)) continue
            if (isSubtype(element, provider as TypeElement, named)) {

                implementations.add(createProvider(provider, typeElement, target, parents = parents))
            }
        }

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@"+(if(named == null) "Default" else named)}]")
                    .setElement(element)
        }

        if (implementations.isNotEmpty()) {
            parents.add(implementations[0].returnType())
            return implementations
        }

        if (implementations.size > 1) {
            throw ProcessorException("Ambiguous classesWithDependencyAnnotation for type [${element.asType()}] with qualifiers [${if (named?.isBlank() == true) "@Default" else "@"+(if(named == null) "Default" else named)}]")
                    .setElement(element)
        }

        if ((isInterface || isAbstractClass) && implementations.isEmpty()) {
            throw ProcessorException("Can't find implementations of `${element.asType()} $element` forTarget: ${target.element} maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method.").setElement(element)
                    .setElement(element)
        }

        return implementations
    }

    private fun findMethodProviders(element: Element, named: String?, parents: ParensSet, typeElement: Element, target: TargetType, implementations: MutableList<DependencyProvider>) {
        for (provider in methodsWithDependencyAnnotation()) {

            // if method is abstract skip
            if (provider.modifiers.contains(Modifier.ABSTRACT)) continue


            if (!provider.returnType.isEqualTo(element) || !qualifierFinder.hasNamed(named, provider)) continue

            val returnType = provider.returnType.asDeclared().asTypeElement()

            if (parents.containsAny(provider.parameters)) {
                val params = provider.parameters.joinToString(prefix = "(", postfix = ")") { it.asType().toString() }
                // TODO cyclic
                //throw ProcessorException("Cyclic graph searching: $returnType parents: $parents parameters: $params")
            }

            val type = createProvider(returnType, typeElement, target, provider.parameters, isMethod = true, parents = parents)
            type.isMethod = true
            type.scoped = ScopeFinder.getScope(provider) ?: type.scoped
            type.name = provider.simpleName.toString()
            type.module = ClassName.get(provider.enclosingElement.asTypeElement())
            type.isSingleton = type.isSingleton || provider.isHasAnnotation(Singleton::class.java)
            if (type.isSingleton) {
                type.packageName = returnType.getPackage().toString()
                if (type.dependencyModels.any { it.asTarget }) {
                    throw ProcessorException("target can't be user as dependency in Singleton").setElement(provider)
                }
            }
            implementations.add(type)
        }
    }

    private fun createProvider(implementation: TypeElement,
                               parent: Element,
                               target: TargetType,
                               dependencies: List<Element> = emptyList(),
                               isMethod: Boolean = false,
                               parents: ParensSet) : DependencyProvider {

        val modulePackageName = implementation.getPackage().toString()

        val named = qualifierFinder.getQualifier(implementation)
        val scoped = ScopeFinder.getScope(implementation) ?: ROOT_SCOPE
        val provider = DependencyProvider(implementation, implementation.isHasAnnotation(Singleton::class.java), ClassName.get(implementation))
        provider.named = named
        provider.scoped = scoped
        provider.isMethod = false
        if (provider.isSingleton) {
            provider.packageName = modulePackageName
        }

        provider.returnTypes.add(implementation.asType())
        collectSuperTypes(implementation, provider.returnTypes)

        if (dependencies.isNotEmpty()) {
            for (dependency in dependencies) {
                if (dependency.isEqualTo(parent.asType())) {
                    throw ProcessorException("Cyclic graph detected building ${dependency.asType()} cyclic: ${dependency.asType()}").setElement(parent)
                }
                val resolved = dependencyResolver.resolveDependency(dependency, target = target, parents = parents)
                provider.dependencyModels.add(resolved)
            }
            return provider
        }

        if (isMethod) return provider

        val constructor = implementation.argumentsConstructor() ?: return provider

//        if (parents.containsAny(constructor.parameters)) {
//            val params = constructor.parameters.joinToString(prefix = "(", postfix = ")") { it.asType().toString() }
//            throw ProcessorException("Cyclic graph creating: $implementation parents: $parents parameters: $params")
//        }

        IProcessor.singletons["${provider.method.asType()}"]?.let {
            provider.dependencyModels.addAll(it)
            return provider
        }

        for (parameter in constructor.parameters) {
            if (parameter.isEqualTo(parent.asType())) {
                throw ProcessorException("Cyclic graph detected building ${parameter.asType()} cyclic: ${parameter.asType()}").setElement(parent)
            }
            val dependency = dependencyResolver.resolveDependency(parameter, target = target, parents = parents)
            provider.dependencyModels.add(dependency)
            if (provider.isSingleton) {
                IProcessor.singletons["${provider.method.asType()}"] = provider.dependencyModels
            }
        }

        return provider
    }




    fun collectSuperTypes(typeElement: TypeElement?, returnTypes: MutableList<TypeMirror>) {
        typeElement ?: return

        for (typeInterface in typeElement.interfaces) {
            returnTypes.add(typeInterface)
            collectSuperTypes(typeInterface.asTypeElement(), returnTypes)
        }
        if (typeElement.superclass.isNotValid()) return
        returnTypes.add(typeElement.superclass)
        typeElement.superclass?.let {
            if (it.kind != TypeKind.NONE) {
                collectSuperTypes(it.asTypeElement(), returnTypes)
            }
        }
    }

    companion object {
        // We have a dependency that needs to check with all superclasses and their interfaces
        fun isSubtype(dependency: Element, implementation: TypeElement, named: String? = null): Boolean {
            return compareSuperclasses(dependency, named, implementation.asType())
        }

        private fun compareSuperclasses(compareElement: Element, named: String?, superclass: TypeMirror?) : Boolean {
            superclass ?:  return false
            if (superclass.isNotValid()) return false
            val typeElement = superclass.asTypeElement()
            val typeElementNamed = qualifierFinder.getQualifier(typeElement)
            if (typeElement.isEqualTo(compareElement) && named == typeElementNamed) return true
            if (typeElement.interfaces.any { compareInterfaces(compareElement, it) } && named == typeElementNamed) return true
            return compareSuperclasses(compareElement, named, typeElement.superclass)
        }

        private fun compareInterfaces(compareElement: Element, checkingInterface: TypeMirror?) : Boolean {
            checkingInterface ?:  return false
            if (checkingInterface.isEqualTo(compareElement)) return true
            return checkingInterface.asTypeElement().interfaces.any { compareInterfaces(compareElement, it) }
        }
    }

}