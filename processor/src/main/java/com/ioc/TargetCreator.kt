package com.ioc

import com.ioc.common.*
import javax.lang.model.element.TypeElement

fun createTarget(element: TypeElement, dependencyFinder: DependencyTypesFinder): TargetType {
    val type = TargetType(element)

    type.postInitialization = postInitializationMethod(element)
    type.supertypes.addAll(dependencyFinder.collectSuperTypes(type.element))

    type.asTargetDependencies.add(element.asType().toString())
    for (supertype in type.supertypes) {
        type.asTargetDependencies.add(supertype.toString())
    }

    // find all localScoped dependencies for use it later
    val found = scanForAnnotation(element, localScopeJavaType)
    for (localScoped in found) {
        var localScopeType = localScoped.asType().toString()
        val getterName = if (localScoped.isMethod()) {
            localScopeType = localScoped.asMethod().returnType.toString()
            localScoped.toGetterName()
        } else {
            findDependencyGetter(localScoped)
                .orElse { throwsGetterIsNotFound(localScoped) }
                .toGetterName()
        }
        type.localScopeDependencies[localScopeType] = getterName
    }

    // get first superclass
    type.superclass?.let {
        type.parentTarget = createTarget(it.asTypeElement(), dependencyFinder)
    }

    return type
}
