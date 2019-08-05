//package com.ioc
//
//import com.ioc.common.*
//import javax.annotation.processing.RoundEnvironment
//import javax.lang.model.element.*
//import javax.lang.model.type.TypeMirror
//import javax.lang.model.util.Types
//
///**
// * Created by sergeygolishnikov on 14/12/2017.
// */
//class CyclicValidation(val types: Types,
//                       val roundEnv: RoundEnvironment,
//                       val target: TargetType) {
//
//    private val cachedImplementations = mutableMapOf<String, MutableList<Element>>()
//    val constructorDependsOf = mutableMapOf<TypeMirror, MutableList<TypeMirror>>()
//    val fieldsDependsOf = mutableMapOf<TypeMirror, MutableList<TypeMirror>>()
//
//    @Throws(ProcessorException::class)
//    fun validate(element: Element) {
//
//        if (!element.isMethod() && element.asTypeElement().isInterface()) {
//            val methodProvider = findAllImplementations(element)
//            for (implementation in methodProvider) {
//                validateConstructorCyclic(implementation, element)
//            }
//            return
//        }
//
//        validateConstructorCyclic(element, element)
//    }
//
//    private fun findAllImplementations(element: Element): List<Element> {
//        cachedImplementations[element.asType().toString()]?.let { return it }
//
//        val methodProvider = mutableListOf<Element>()
//        for (rootElement in roundEnv.rootElements) {
//            if (rootElement.kind == ElementKind.METHOD) continue
//            if (rootElement.kind == ElementKind.ANNOTATION_TYPE) continue
//            if (rootElement.isEqualTo(element)) continue
//            val packageName = rootElement.asTypeElement().getPackage().toString()
//            if (excludedPackages.any { packageName.startsWith(it) } && !TargetChecker.isSubtype(target, rootElement)) {
//                continue
//            }
//
//            if (rootElement.modifiers.contains(Modifier.ABSTRACT)) continue
//
//            if (DependencyTypesFinder.isSubtype(element, rootElement as TypeElement)) {
//                methodProvider.add(rootElement)
//            }
//        }
//
//        cachedImplementations[element.asType().toString()] = methodProvider
//
//        return methodProvider
//    }
//
//
//    @Throws(ProcessorException::class)
//    private fun validateConstructorCyclic(element: Element, originalElement: Element) {
//
//        var typeElement: Element = element
//        if (typeElement.isWeak() /*|| typeElement.isProvider(types)*/) {
//            typeElement = typeElement.getGenericFirstType().asTypeElement()
//        }
//
//        if (typeElement is ExecutableElement) {
//            typeElement = typeElement.parameters[0].asTypeElement()
//        }
//
//        val argumentConstructor = typeElement.constructor()
//        val parameters = argumentConstructor?.parameters ?: emptyList()
//        for (parameter in parameters) {
//            // Skip all constructors with primitive types
//            if (!parameter.isSupportedType()) continue
//            if (TargetChecker.isSubtype(target, parameter)) continue
//
//            if (parameter.asTypeElement().isInterface()) {
//                val methodProvider = findAllImplementations(parameter)
//                for (implementation in methodProvider) {
//
//                    if (checkConstructors(constructorDependsOf[parameter.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${parameter.asType()}").setElement(typeElement)
//                    }
//
//                    if (checkConstructors(constructorDependsOf[implementation.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${parameter.asType()}").setElement(typeElement)
//                    }
//
//                    if (checkFields(fieldsDependsOf[parameter.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${parameter.asType()}").setElement(typeElement)
//                    }
//
//                    if (checkFields(fieldsDependsOf[implementation.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${parameter.asType()}").setElement(typeElement)
//                    }
//
//                    val list = constructorDependsOf.getOrPut(originalElement.asType()) { mutableListOf() }
//                    list.add(implementation.asType())
//                    list.add(parameter.asType())
//                    validateConstructorCyclic(implementation, parameter)
//                }
//                continue
//            }
//
//            if (checkFields(fieldsDependsOf[parameter.asType()], typeElement.asType())) {
//                throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${parameter.asType()}").setElement(typeElement)
//            }
//
//            if (checkConstructors(constructorDependsOf[parameter.asType()], typeElement.asType())) {
//                throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${parameter.asType()}").setElement(typeElement)
//            }
//            val list = constructorDependsOf.getOrPut(originalElement.asType()) { mutableListOf() }
//            list.add(parameter.asType())
//            validateConstructorCyclic(parameter, parameter)
//        }
//
//        val injectionFields = typeElement.injectionFields()
//        for (field in injectionFields) {
//            // Skip all constructors with primitive types
//            if (!field.isSupportedType()) continue
//            if (TargetChecker.isSubtype(target, field)) continue
//
//            if (field.asTypeElement().isInterface()) {
//                val methodProvider = findAllImplementations(field)
//                val list = fieldsDependsOf.getOrPut(originalElement.asType()) { mutableListOf() }
//                for (implementation in methodProvider) {
//                    if (checkConstructors(constructorDependsOf[field.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${field.asType()}").setElement(typeElement)
//                    }
//
//                    if (checkConstructors(constructorDependsOf[implementation.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${field.asType()}").setElement(typeElement)
//                    }
//
//                    if (checkFields(fieldsDependsOf[field.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${field.asType()}").setElement(typeElement)
//                    }
//
//                    if (checkFields(fieldsDependsOf[implementation.asType()], typeElement.asType())) {
//                        throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${field.asType()}").setElement(typeElement)
//                    }
//
//                    list.add(implementation.asType())
//                    validateConstructorCyclic(implementation, field)
//                }
//                continue
//            }
//
//            if (checkConstructors(constructorDependsOf[field.asType()], typeElement.asType())) {
//                throw ProcessorException("Cyclic graph detected building ${typeElement.asType()} cyclic: ${field.asType()}").setElement(typeElement)
//            }
//
//            val list = fieldsDependsOf.getOrPut(originalElement.asType()) { mutableListOf() }
//            list.add(field.asType())
//            validateConstructorCyclic(field, field)
//        }
//    }
//
//    private fun checkConstructors(possibles: MutableList<TypeMirror>?, checkType: TypeMirror): Boolean {
//        possibles ?: return false
//        if (possibles.contains(checkType) && !TargetChecker.isSubtype(target, checkType.asTypeElement())) return true
//
//        possibles.forEach {
//            if (checkConstructors(constructorDependsOf[it], checkType) && !TargetChecker.isSubtype(target, it.asTypeElement())) return true
//        }
//
//        return false
//    }
//
//    private fun checkFields(possibles: MutableList<TypeMirror>?, checkType: TypeMirror): Boolean {
//        possibles ?: return false
//        if (possibles.contains(checkType) && !TargetChecker.isSubtype(target, checkType.asTypeElement())) return true
//
//        possibles.forEach {
//            if (checkFields(fieldsDependsOf[it], checkType) && !TargetChecker.isSubtype(target, it.asTypeElement())) return true
//        }
//
//        return false
//    }
//}