package com.ioc

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