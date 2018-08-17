package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

/**
 * Created by sergeygolishnikov on 12/04/2017.
 */

object ProviderAnonymousClass {
    operator fun get(methodBody: CodeBlock, parameterizedType: TypeName): TypeSpec {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(providerType, parameterizedType))
                .addMethod(MethodSpec.methodBuilder("get")
                        .addAnnotation(Override::class.java)
                        .addAnnotation(nonNullAnnotation)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(parameterizedType)
                        .addCode(methodBody)
                        .build())
                .build()
    }
}

object LazyAnonymousClass {
    operator fun get(methodBody: CodeBlock, name: String, type: Element): TypeSpec {

        val code = CodeBlock.builder()
        code.addStatement("if (isInitialized()) return value")
        code.add(methodBody)
        code.addStatement("value = \$N", name)
        code.addStatement("return value")
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(lazy(type))
                .addField(FieldSpec.builder(ClassName.get(type.asType()), "value", Modifier.PRIVATE).build())
                .addMethod(MethodSpec.methodBuilder("isInitialized")
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return value != null")
                        .returns(TypeName.BOOLEAN).build())
                .addMethod(MethodSpec.methodBuilder("get")
                        .addAnnotation(Override::class.java)
                        .addAnnotation(nonNullAnnotation)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(type.asTypeName())
                        .addCode(code.build())
                        .build())
                .build()
    }
}


object ViewModelFactoryAnonymousClass {
    operator fun get(methodBody: CodeBlock): TypeSpec {

        val code = CodeBlock.builder()

        code.add(methodBody)

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(viewModelFactoryType)
                .addMethod(MethodSpec.methodBuilder("create")
                        .addAnnotation(nonNullAnnotation)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.bestGuess("java.lang.Class"), TypeVariableName.get("T")), "modelClass", Modifier.FINAL).addAnnotation(nonNullAnnotation).build())
                        .addTypeVariable(TypeVariableName.get("T", viewModelType))
                        .returns(TypeVariableName.get("T"))
                        .addCode(code.build())
                        .build())
                .build()
    }
}
