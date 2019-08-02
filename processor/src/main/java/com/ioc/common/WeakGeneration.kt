package com.ioc.common

import com.ioc.DependencyModel
import com.squareup.javapoet.CodeBlock

object WeakGeneration {
    fun wrapInWeakIfNeed(model: DependencyModel, body: CodeBlock.Builder): CodeBlock.Builder {
        if (!model.isWeakDependency) return body
        val weakType = model.originalType.asWeakType()
        val originalName = model.generatedName
        model.generatedName = "weak${model.generatedName.capitalize()}"
        val wrappedCode = CodeBlock.builder().addStatement("\$T \$N = new \$T(\$N)",
            weakType,
            model.generatedName,
            weakType,
            originalName)
        body.add(wrappedCode.build())
        return body
    }
}