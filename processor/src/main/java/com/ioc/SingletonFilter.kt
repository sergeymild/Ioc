package com.ioc

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
            findAll(model.dependencies, singletons, uniqueSingletons)
        }
    }
}