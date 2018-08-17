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

fun uniqueSingleton(base: CharSequence): Boolean {
    var list = uniqueSingletons[base.toString()]
    if (list == null) {
        list = mutableSetOf()
        uniqueSingletons[base.toString()] = list
    }
    return list.add(base.toString())
}

fun getOriginalSingletonName(base: CharSequence): String {
    for (key in uniqueSingletons.keys) {
        if (uniqueSingletons[key]?.contains(base.toString()) == true) {
            return key.decapitalize()
        }
    }
    return base.toString().decapitalize()
}


/**
 * Adds `name` without any modification to the name set. Has no effect if `name` is
 * already present in the set.
 */
fun claim(name: CharSequence) {
    uniqueNames.add(name.toString())
}