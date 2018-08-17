package com.ioc

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */
object PackageResolver {
    fun resolve(packages: Set<String>?): String? {
        packages ?: return null
        val fold = packages.fold(mutableMapOf<Int, Int>()) { map, string ->
            val size = string.split(".").size
            val mapSize = map[size]
            map[size] = if (mapSize == null) 1 else mapSize + 1
            map
        }

        val shortName = packages.filter { !it.isNullOrEmpty() }
                .map { Pair(it, it.split(".").size) }
                .minBy { it.second } ?: return null
        val countSize = fold[shortName.second]
        if (countSize != null && countSize > 1) {
            return shortName.first.split(".").dropLast(1).joinToString(".")
        }
        return shortName.first
    }
}