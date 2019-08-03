package com.ioc

import java.util.*


/**
 * Created by sergeygolishnikov on 28/11/2017.
 */
object SingletonFilter {

    fun findAll(
        dependencyModel: List<DependencyModel>,
        singletons: MutableList<SingletonWrapper>,
        uniqueSingletons: MutableSet<String>) {

        val queue = LinkedList(dependencyModel)

        while (queue.isNotEmpty()) {
            val model = queue.pop()

            if (model.isSingleton && uniqueSingletons.add(model.originalTypeString)) {
                singletons.add(model)
            }

            queue.addAll(model.dependencies)

            model.methodProvider?.let {
                if (it.isSingleton && uniqueSingletons.add(model.originalTypeString)) {
                    singletons.add(model)
                }
                queue.addAll(it.dependencyModels)
            }
        }
    }
}