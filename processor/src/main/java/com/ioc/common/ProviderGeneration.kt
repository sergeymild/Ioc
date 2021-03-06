package com.ioc.common

import com.ioc.DependencyModel
import com.ioc.asProviderType
import com.ioc.asTypeName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

object ProviderGeneration {
    fun wrapInProviderClassIfNeed(model: DependencyModel, body: CodeBlock.Builder): CodeBlock.Builder {
        if (!model.isProvider) return body
        val originalName = model.generatedName
        model.generatedName = "provider${model.generatedName.titleize()}"
        return CodeBlock
            .builder()
            .add("\$T \$N = \$L;\n",
                model.dependency.asProviderType(),
                model.generatedName,
                anonymousClass(model.dependency, originalName, body.build()))
    }


    fun wrapProvideMethod(model: DependencyModel, body: CodeBlock): CodeBlock {
        if (!model.isProvider) return body
        return CodeBlock.builder()
            .add("\$L", TypeSpec.anonymousClassBuilder("")
                .superclass(model.dependency.asProviderType())
                .addMethod(MethodSpec.methodBuilder("initialize")
                    .addModifiers(Modifier.PROTECTED)
                    .returns(model.dependency.asTypeName())
                    .addStatement("return \$L", body)
                    .build())
                .build())
            .build()
    }


    private fun providerMethod(type: Element, name: CharSequence, body: CodeBlock): MethodSpec {
        return MethodSpec.methodBuilder("initialize")
            .addModifiers(Modifier.PROTECTED)
            .returns(type.asTypeName())
            .addCode(body)
            .addStatement("return \$N", name)
            .build()
    }

    private fun anonymousClass(type: Element, name: CharSequence, body: CodeBlock): TypeSpec {
        return TypeSpec.anonymousClassBuilder("")
            .superclass(type.asProviderType())
            .addMethod(providerMethod(type, name, body))
            .build()
    }
}