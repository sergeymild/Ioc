package com.ioc

import com.ioc.common.keepAnnotation
import com.ioc.common.nullableAnnotation
import com.ioc.common.singletonClassName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Created by sergeygolishnikov on 28/11/2017.
 */

class NewSingletonSpec(private val dependency: DependencyModel) {

    @Throws(Throwable::class)
    fun createSpec(): TypeSpec {

        val singletonName = singletonClassName(dependency)
        return TypeSpec.classBuilder(singletonName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(dependency.asLazyType())
                .addAnnotation(keepAnnotation)
                .addMethod(getInstanceMethod(singletonName))
                .addMethod(initializeMethod())
                .addMethod(generateClearMethod())
                .addField(FieldSpec.builder(ClassName.bestGuess(singletonName), "instance", Modifier.PRIVATE, Modifier.STATIC).addAnnotation(nullableAnnotation).build())
                .build()
    }

    private fun getInstanceMethod(singletonName: String): MethodSpec {
        return MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .returns(dependency.originalClassName)
                .addStatement("if (instance == null) instance = new \$N()", singletonName)
                .addStatement("return instance.get()")
                .build()
    }

    private fun initializeMethod(): MethodSpec {
        val builder = MethodSpec.methodBuilder("initialize")
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                .returns(dependency.originalClassName)

        val code = DependencyTree.get(dependency.dependencies)
        builder.addCode(code)

        applyIsLoadIfNeed(dependency.dependencies, null)
        val names = dependency.dependencyNames()

        dependency.methodProvider?.let {
            val instance = if (it.isKotlinModule) "INSTANCE." else ""
            return builder.addStatement("return \$T.$instance\$N(\$L)", it.module, it.name, names).build()
        }
        builder.addStatement("return new \$T(\$L)", dependency.originalClassName, names)
        return builder.build()
    }

    private fun generateClearMethod(): MethodSpec {
        val cleanUp = IProcessor.elementUtils.getTypeElement(Cleanable::class.java.canonicalName)

        val builder = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .addAnnotation(keepAnnotation)

        if (IProcessor.types.isSubtype(dependency.dependency.asType(), cleanUp.asType())) {
            builder.addStatement("instance.onCleared()")
        }

        builder.addStatement("instance = null")

        return builder.build()
    }

}