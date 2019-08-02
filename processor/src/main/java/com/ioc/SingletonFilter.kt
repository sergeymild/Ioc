package com.ioc

import java.util.*


/**
 * Created by sergeygolishnikov on 28/11/2017.
 */
object SingletonFilter {

    fun findAll(
        dependencyModel: List<DependencyModel>,
        singletons: MutableList<SingletonWrapper>) {

        val uniqueSingletons = mutableSetOf<String>()

        val queue = LinkedList(dependencyModel)

        while (queue.isNotEmpty()) {
            val model = queue.pop()

            if (model.isSingleton && uniqueSingletons.add(model.typeElementString)) {
                singletons.add(model)
            }

            queue.addAll(model.dependencies)

            for (implementation in model.implementations) {
                if (implementation.isSingleton && uniqueSingletons.add(model.typeElementString)) {
                    singletons.add(model)
                }
                queue.addAll(implementation.dependencyModels)
            }
        }
    }
}