package com.ioc

import java.util.*

/**
 * Created by sergeygolishnikov on 04/01/2018.
 */

class Sorting {

    private var orders: MutableMap<String, Int> = HashMap()
    var isLoggerEnabled = false

    fun countOrder(appender: String, root: String, models: List<DependencyModel>, depth: Int) {
//        if (isLoggerEnabled) IProcessor.messager.printMessage(Diagnostic.Kind.WARNING, "depth: $depth -> $root")

        orders[root] = orders.getOrDefault(root, 0) + depth

        if (models.isEmpty()) return

        for (model in models) {
            countOrder(appender + appender, model.typeString, model.dependencies, depth + 1)
        }
    }

    fun sortTargetDependencies(models: List<DependencyModel>) {

        for (model in models) {
            model.sortOrder = orders.getOrDefault(model.typeString, Int.MAX_VALUE)
        }
    }
}
