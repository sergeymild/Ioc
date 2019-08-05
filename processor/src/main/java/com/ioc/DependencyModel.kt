package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun targetDependencyModel(element: Element): DependencyModel {
    val dependency = DependencyModel(element, element.asTypeElement()/*, element.simpleName*/)
    dependency.generatedName = "target"
    return dependency
}

fun DependencyModel.isAllowEmptyConstructorInjection(): Boolean {
    return methodProvider == null
        && (constructor != null && !constructor!!.modifiers.contains(Modifier.PRIVATE))
        && dependencies.isEmpty()
        && !isProvider
        && !isLazy
        && !isWeak
        && !isViewModel
}

fun DependencyModel.isAllowModuleMethodProvide(): Boolean {
    val method = methodProvider
    return method != null
        && method.dependencies.isEmpty()
        && !isProvider
        && !isLazy
        && !isWeak
        && !isViewModel
}

fun DependencyModel.returnType(): TypeName {
    return when {
        isLazy -> dependency.asLazyType()
        isProvider -> dependency.asProviderType()
        isWeak -> dependency.asWeakType()
        else -> originalClassName
    }
}

fun DependencyModel.copy(): DependencyModel {
    return DependencyModel(
        dependency,
        originalType,
        fieldName,
        isProvider,
        isLazy,
        isWeak,
        isSingleton,
        isViewModel,
        isLocal
    ).also {
        it.typeArguments = typeArguments
        it.methodProvider = methodProvider
        it.constructor = constructor
        it.sortOrder = sortOrder
        it.named = named
        it.setterMethod = setterMethod
        it.dependencies = dependencies.map { d -> d.copy() }
    }
}

class DependencyModel constructor(
    val dependency: Element,
    val originalType: TypeElement,
    var fieldName: CharSequence = "",
    var isProvider: Boolean = false,
    var isLazy: Boolean = false,
    var isWeak: Boolean = false,
    var isSingleton: Boolean = false,
    var isViewModel: Boolean = false,
    var isLocal: Boolean = false) {

    val typeString by lazy { dependency.asType().toString() }
    val originalTypeString by lazy { originalType.asType().toString() }
    val dependencyTypeString by lazy { dependency.asType().toString() }

    var dependencies: List<DependencyModel> = emptyList()
    var typeArguments = mutableListOf<TypeMirror>()
    var methodProvider: ModuleMethodProvider? = null

    var constructor: ExecutableElement? = null
    var sortOrder = Int.MAX_VALUE
    var named: String? = ""
    var setterMethod: ExecutableElement? = null

    var generatedName: CharSequence = dependency.asTypeElement().simpleName.decapitalize()

    val originalClassName: TypeName by lazy {
        if (typeArguments.isEmpty()) return@lazy ClassName.get(originalType.asTypeElement())
        return@lazy ParameterizedTypeName.get(ClassName.get(originalType.asTypeElement()), *typeArguments.map { ClassName.get(it) }.toTypedArray())
    }

    fun setterName(): CharSequence {
        setterMethod?.let { return it.simpleName.toString() }
        return fieldName
    }

    override fun equals(other: Any?): Boolean {
        return dependency.asType().toString() == (other as? DependencyModel)?.dependency?.asType()?.toString()
    }

    override fun hashCode(): Int {
        return originalClassName.hashCode()
    }

    override fun toString(): String {
        return "type: $dependencyTypeString - originalType: $originalTypeString"
    }
}