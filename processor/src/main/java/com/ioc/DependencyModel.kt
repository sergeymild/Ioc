package com.ioc

import com.ioc.common.asTypeElement
import com.ioc.common.getPackage
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun DependencyModel.copy(): DependencyModel {
    return DependencyModel(dependency, originalType, fieldName, erasuredType, isProvider, isLazy, isWeakDependency)
}

class DependencyModel constructor(var dependency: Element,
                                  var originalType: Element,
                                  var fieldName: String,
                                  var erasuredType: TypeMirror,
                                  var isProvider: Boolean = false,
                                  var isLazy: Boolean = false,
                                  var isWeakDependency: Boolean = false): SingletonWrapper {

    val typeString: String
        get() = dependency.asType().toString()
    var argumentsConstructor: ExecutableElement? = null
    var emptyConstructor: ExecutableElement? = null
    var asTarget: Boolean = false
    var isSingleton: Boolean = false
    var isFromTarget: Boolean = false
    var isLocal: Boolean = false
    var isViewModel: Boolean = false
    var targetMethod: ExecutableElement? = null
    override val typeElement: TypeElement
        get() = dependency.asType().asTypeElement()
    override val packageName: String
        get() = typeElement.getPackage().toString()
    override var name: String = ""
        get() = erasuredType.toString().split(".").last().decapitalize()
        set(value) {
            field = value
        }
    override val className: ClassName
        get() = ClassName.get(typeElement)
    override var depencencies: List<DependencyModel> = emptyList()
        get() {
            return if (implementations.isEmpty()) field
            else implementations[0].dependencyModels
        }
    var typeArguments = mutableListOf<TypeMirror>()
    override var implementations: List<DependencyProvider> = emptyList()
    fun provideMethod(): DependencyProvider? {
        return implementations.firstOrNull { it.isMethod }
    }

    //var packageName: String? = null
    var order: Int = Int.MAX_VALUE
    var injectMethodName: String = "inject${name.capitalize()}In${fieldName.capitalize()}"
    var cacheMethodName: String = ""
        get() = "cache${name.capitalize()}"
    var named: String? = ""
    var scoped: String? = ROOT_SCOPE
    var setterMethod: ExecutableElement? = null
    var scopedFieldName: String = dependency.asTypeElement().simpleName.toString()

    var generatedName: String = name
    var scopedName: String = ""
        get() {
            return dependency.asTypeElement().simpleName.toString()
        }

    override fun originalClassName() : TypeName {
        if (typeArguments.isEmpty()) return ClassName.get(dependency.asTypeElement())
        return ParameterizedTypeName.get(ClassName.get(dependency.asTypeElement()) , *typeArguments.map { ClassName.get(it) }.toTypedArray())
    }

    fun setterName(): String {
        return if (setterMethod != null) setterMethod!!.simpleName.toString() else fieldName
    }

    override fun equals(other: Any?): Boolean {
        return dependency.asType().toString() == (other as? DependencyModel)?.dependency?.asType()?.toString()
    }

    override fun hashCode(): Int {
        return className.hashCode()
    }

    fun setDependency(target: String, value: String, vararg arguments: Any): CodeBlock {
        if (setterMethod != null) {
            return CodeBlock.builder().addStatement("$target($value)", *arguments).build()
        }
        return CodeBlock.builder().addStatement("$target = $value", *arguments).build()
    }
}