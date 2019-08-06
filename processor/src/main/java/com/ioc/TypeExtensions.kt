package com.ioc

import com.ioc.IProcessor.Companion.qualifierFinder
import com.ioc.common.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName

import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter

fun Element.asTypeName(): TypeName {
    return ClassName.get(asType())
}

fun Element.asLazyType(): TypeName {
    return ParameterizedTypeName.get(iocLazyType, asTypeName())
}

fun Element.asProviderType(): TypeName {
    return ParameterizedTypeName.get(iocProviderType, asTypeName())
}

fun Element.asWeakType(): TypeName {
    return ParameterizedTypeName.get(weakType, asTypeName())
}

fun postInitializationMethod(element: TypeElement): ExecutableElement? {
    val postInitializationMethod = element.methods { it.isHasAnnotation(PostInitialization::class.java) }.firstOrNull()
    if (postInitializationMethod != null && postInitializationMethod.isPrivate()) {
        throw ProcessorException("@PostInitialization placed on `${postInitializationMethod.simpleName}` in ${postInitializationMethod.enclosingElement} with private access").setElement(postInitializationMethod)
    }

    if (postInitializationMethod != null && postInitializationMethod.parameters.isNotEmpty()) {
        throw ProcessorException("@PostInitialization placed on `${postInitializationMethod.simpleName}` in ${postInitializationMethod.enclosingElement} must not have parameters").setElement(postInitializationMethod)
    }

    if (postInitializationMethod != null && postInitializationMethod.returnType.kind != TypeKind.VOID) {
        throw ProcessorException("@PostInitialization placed on `${postInitializationMethod.simpleName}` in ${postInitializationMethod.enclosingElement} must not have return type").setElement(postInitializationMethod)
    }
    return postInitializationMethod
}

fun Element.asTypeString(): String = asType().toString()

fun ExecutableElement.firstParameter(): TypeMirror {
    return parameters.first().asType()
}

fun Element.findViewModels(): List<VariableElement> {
    return ElementFilter.fieldsIn(enclosedElements)
        .filter { it.isViewModel() || it.isAndroidViewModel() }
}

fun Element.findLiveData(): List<VariableElement> {
    return ElementFilter.fieldsIn(enclosedElements)
        .filter { it.isLiveData() }
}

fun Element.methods(predicate: (ExecutableElement) -> Boolean): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements).filter(predicate)
}

fun Element.methods(): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements)
}

fun findDataObservers(element: TypeElement): List<TargetDataObserver> {
    val targetDataObservers = mutableListOf<TargetDataObserver>()
    val observerMethods = element.methods { it.isHasAnnotation(DataObserver::class.java) }.toMutableList()
    if (observerMethods.isEmpty()) return emptyList()
    observerMethods.forEach(::validateObserverMethod)
    validateTargetForLiveDataObserver(element)
    val viewModels = element.findViewModels()
    viewModels.forEach(::validateViewModelForLiveData)

    for (viewModel in viewModels) {
        val viewModelType = viewModel.asTypeElement()
        val viewModelLiveDataFields = viewModelType.findLiveData()
        for (viewModelLiveDataField in viewModelLiveDataFields) {
            val liveDataNamed = qualifierFinder.getQualifier(viewModelLiveDataField) ?: "unset"
            val liveDataGeneric = viewModelLiveDataField.getGenericFirstType()
            for (observerMethod in observerMethods) {
                val observerMethodNamed = qualifierFinder.getQualifier(observerMethod) ?: "unset"
                if (observerMethod.firstParameter().toString() == liveDataGeneric.asType().toString() && liveDataNamed == observerMethodNamed) {
                    targetDataObservers.add(TargetDataObserver(
                        viewModel = viewModelType,
                        targetViewModelField = findDependencyGetter(viewModel),
                        viewModelLiveDataField = findDependencyGetterFromTypeOrSuperType(viewModelLiveDataField),
                        observingType = liveDataGeneric,
                        observerMethod = observerMethod,
                        observeType = observerMethod.getAnnotation(DataObserver::class.java).value
                    ))
                    observerMethods.remove(observerMethod)
                    break
                }
            }
        }
    }

    if (observerMethods.isNotEmpty()) {
        throw ProcessorException("$element contains methods [${observerMethods.joinToString { it.simpleName }}] which annotated as @DataObserver but didn't find any view models with LiveData.").setElement(element)
    }

    return targetDataObservers
}
