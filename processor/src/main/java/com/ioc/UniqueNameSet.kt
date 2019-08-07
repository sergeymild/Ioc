package com.ioc

import java.util.*
import kotlin.collections.HashSet

/**
 * Created by sergeygolishnikov on 22/12/2017.
 */

private val uniqueNames = HashSet<String>()

/**
 * Generates a unique name using `base`. If `base` has not yet been added, it will be
 * returned as-is. If your `base` is healthy, this will always return `base`.
 */

fun resetUniqueNames() {
    uniqueNames.clear()
}

fun uniqueName(model: DependencyModel): CharSequence {
    if (model.generatedName == "target") return model.generatedName
    var name = model.generatedName.toString().decapitalize()
    var differentiator = 2
    while (!uniqueNames.add(name)) {
        name = model.generatedName.toString() + differentiator
        differentiator++
    }
    model.generatedName = name
    return name
}

fun generateUniqueNamesForInjectMethodDependencies(target: TargetType?, models: List<DependencyModel>) {
    resetUniqueNames()
    val queue = LinkedList(models)

    while (queue.isNotEmpty()) {
        val dep = queue.pop()
        if (!dep.isSingleton &&
            !target.isSubtype(dep.dependency, dep.originalType) &&
            !target.isLocalScope(dep.dependency, dep.originalType)) {

            dep.generatedName = uniqueName(dep)
        }
        if (!dep.isSingleton) queue.addAll(dep.dependencies)
    }
}
