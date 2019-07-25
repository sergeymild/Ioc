package com.ioc

import com.ioc.common.asTypeElement
import com.ioc.common.isEqualTo
import com.ioc.common.isInterface
import com.squareup.javapoet.ClassName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */



class TargetType(val element: TypeElement) {
    var name = element.simpleName.toString()
    var className = ClassName.get(element)
    var flatDependencies = emptyList<DependencyModel>()
    var dependencies = emptyList<DependencyModel>()
    var parentTarget: TargetType? = null
    var childTarget: TargetType? = null
    var postInitialization: ExecutableElement? = null
    var supertypes = mutableListOf<TypeMirror>()
    var localScopeDependencies = mutableMapOf<String, String>()

    val superclass: TypeMirror?
        get() = supertypes.firstOrNull { !it.asTypeElement().isInterface() }

    var parentDependencies = mutableListOf<DependencyModel>()

    fun uniqueFlat(): MutableSet<DependencyModel> {
        val uniqueDependencies = mutableSetOf<DependencyModel>()
        flatDependencies.forEach { uniqueDependencies.add(it) }
        return uniqueDependencies
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TargetType

        if (element != other.element) return false

        return true
    }

    fun isDeclaredAsMember(dependency: DependencyModel): Boolean {
        return flatDependencies.filter { it.dependency.isEqualTo(dependency.dependency) }.any()
    }


    fun parentsDependencies() {
        parentDependencies.clear()
        var parent = parentTarget
        while (parent != null) {
            parentDependencies.addAll(parent.dependencies)
            parent = parent.parentTarget
        }
    }

    fun findParent(superType: TypeMirror): TargetType? {
        var parent = parentTarget
        while (parent != null) {
            if (parent.element.isEqualTo(superType)) {
                return parent
            }
            parent = parent.parentTarget
        }
        return null
    }

    fun firstParentWithDependencies(): TargetType? {
        var parent = parentTarget
        while (parent != null) {
            if (parent.dependencies.isNotEmpty()) return parent
            parent = parent.parentTarget
        }
        return null
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }

    override fun toString(): String {
        return "TargetType(element=${element.asType()})"
    }


}