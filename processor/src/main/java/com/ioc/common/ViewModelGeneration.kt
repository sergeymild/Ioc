package com.ioc.common

import com.ioc.DependencyModel
import com.ioc.dependencyNames
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

object ViewModelGeneration {
    fun wrapInAndroidViewModelIfNeed(model: DependencyModel, body: CodeBlock.Builder): CodeBlock.Builder {
        if (!model.isViewModel) return body
        val code = CodeBlock.builder().also { it.add(body.build()) }
        val names = model.dependencyNames()
        code.addStatement("return (T) new \$T(\$L)", model.originalClassName, names)
        val originalGeneratedName = model.generatedName
        val factoryName = "factory_${model.generatedName}"

        val viewModelBuilder = viewModelFactoryCode(originalGeneratedName, code)

        viewModelBuilder.addStatement("\$T \$N = \$T.of(target, \$N).get(\$T.class)",
            model.originalClassName,
            model.generatedName,
            viewModelProvidersType,
            factoryName,
            model.originalType)
        return viewModelBuilder
    }

    private fun viewModelFactoryCode(name: CharSequence, code: CodeBlock.Builder): CodeBlock.Builder {
        return CodeBlock.builder().add("\$T \$N = \$L;\n",
            viewModelFactoryType,
            "factory_$name",
            TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(viewModelFactoryType)
                .addMethod(MethodSpec.methodBuilder("create")
                    .addAnnotation(nonNullAnnotation)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(javaClassType, "modelClass", Modifier.FINAL).addAnnotation(nonNullAnnotation).build())
                    .addTypeVariable(TypeVariableName.get("T", viewModelType))
                    .returns(TypeVariableName.get("T"))
                    .addCode(code.build())
                    .build())
                .build())
    }

}