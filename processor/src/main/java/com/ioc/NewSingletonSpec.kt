package com.ioc

import com.ioc.common.keepAnnotation
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
            .addSuperinterface(dependency.asProviderType())
            .addAnnotation(keepAnnotation)
            .addMethod(initializeMethod())
            .build()
    }

    private fun initializeMethod(): MethodSpec {
        val builder = MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(dependency.originalClassName)

        val code = DependencyTree.get(dependency.dependencies)
        builder.addCode(code)

        applyIsLoadIfNeed(dependency.dependencies, null)
        val names = dependency.dependencyNames()

        dependency.methodProvider?.let {
            val instance = if (it.isKotlinModule) "INSTANCE." else ""
            return builder.addStatement("return \$T.$instance\$N(\$L)", it.module, it.name, names).build()
        }
        postInitializationMethod(dependency.originalType)?.let {
            builder.addStatement("\$T instance = new \$T(\$L)", dependency.originalClassName, dependency.originalClassName, names)
            builder.addStatement("instance.\$N()", it.simpleName)
            builder.addStatement("return instance")
            return builder.build()
        }

        builder.addStatement("return new \$T(\$L)", dependency.originalClassName, names)
        return builder.build()
    }
}