package com.ioc

import javax.lang.model.element.Element
import javax.lang.model.type.DeclaredType


/**
 * Created by sergeygolishnikov on 28/11/2017.
 */
object SingletonFilter {

    fun findAll(dependencyModel: List<DependencyModel>, singletons: MutableList<SingletonWrapper>, uniqueSingletons: MutableSet<String>) {

        for (model in dependencyModel) {
            if (model.isSingleton && uniqueSingletons.add((model.dependency.asType() as DeclaredType).asElement().simpleName.toString().toLowerCase())) {
                singletons.add(model)
            }
            for (implementation in model.implementations) {
                findAll(implementation.dependencyModels, singletons, uniqueSingletons)
                if (implementation.isSingleton && uniqueSingletons.add((model.dependency.asType() as DeclaredType).asElement().simpleName.toString().toLowerCase())) {
                    singletons.add(model)
                }
            }
            findAll(model.depencencies, singletons, uniqueSingletons)
        }
    }

    fun findAllScopes(dependency: List<DependencyModel>,
                      scopes: MutableList<DependencyModel>,
                      unique: MutableSet<String>,
                      target: TargetType) {

        for (model in dependency) {
            if (model.scoped == target.rootScope && isEqualScope(model.scoped, model.dependency, unique)) {
                scopes.add(model)
            }
            for (implementation in model.implementations) {
                findAllScopes(implementation.dependencyModels, scopes, unique, target)
                if (implementation.scoped == target.rootScope && isEqualScope(implementation.scoped, model.dependency, unique)) {
                    scopes.add(model)
                }
            }
            findAllScopes(model.depencencies, scopes, unique, target)
        }
    }

    private fun isEqualScope(scope: String?, dependency: Element, unique: MutableSet<String>) : Boolean {
        val name = (dependency.asType() as DeclaredType).asElement().simpleName.toString().toLowerCase()
        return scope != ROOT_SCOPE && unique.add("$name$scope")
    }
}