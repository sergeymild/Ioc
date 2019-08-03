package com.ioc

class InjectMethodMetadata(
    val model: DependencyModel,
    val target: TargetType) {

    private val usedNames = mutableMapOf<String, CharSequence>()

    fun getUniqueName(model: DependencyModel) : CharSequence {
        var newName = model.generatedName
        if (usedNames.contains(model.originalTypeString)) {
            newName = uniqueName(model)
        }
        usedNames[model.originalTypeString] = newName
        return newName
    }

    fun getLastName(model: DependencyModel): CharSequence {
        return usedNames.getOrDefault(model.originalTypeString, model.generatedName)
    }
}