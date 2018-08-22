package com.ioc

/**
 * Created by sergeygolishnikov on 22/12/2017.
 */

private val uniqueNames = HashSet<String>()
private val uniqueSingletons = HashMap<String, MutableSet<String>>()

/**
 * Generates a unique name using `base`. If `base` has not yet been added, it will be
 * returned as-is. If your `base` is healthy, this will always return `base`.
 */

fun resetUniqueNames() {
    uniqueNames.clear()
}

fun resetUniqueSingletons() {
    uniqueSingletons.clear()
}

fun uniqueName(base: CharSequence): String {
    if (base == "target") return base.toString()
    var name = base.toString().decapitalize()
    var differentiator = 2
    while (!uniqueNames.add(name)) {
        name = base.toString() + differentiator
        differentiator++
    }
    return name
}