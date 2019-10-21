package com.ioc

import com.ioc.common.capitalize
import com.ioc.common.keepAnnotation
import com.ioc.common.nonNullAnnotation
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 28/11/2017.
 */
interface SingletonWrapper {
    val typeElement: TypeElement
    val className: ClassName
    val packageName: String
    var depencencies: List<DependencyModel>
    var implementations: List<DependencyProvider>
    var name: String
    fun originalClassName() : TypeName
}

class NewSingletonSpec(private val dependencyModel: SingletonWrapper,
                       private val typeUtils: Types) {

    @Throws(Throwable::class)
    fun inject(): TypeSpec {

        val name = "${dependencyModel.typeElement.simpleName.capitalize()}Singleton"
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)
                .addMethod(generateMethod())
                .addMethod(generateClearMethod())
                .addField(dependencyModel.className, "singleton", Modifier.PRIVATE, Modifier.STATIC)
                .addField(FieldSpec.builder(ClassName.bestGuess(name), "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new \$N()", name)
                        .build())
                .build()
    }

    private fun generateMethod(): MethodSpec {
        resetUniqueSingletons()
        val builder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)
                .addAnnotation(nonNullAnnotation)
                .returns(dependencyModel.className)
                .addStatement("if (singleton != null) return singleton")

        DependencyTree.get(dependencyModel.depencencies, typeUtils).also { builder.addCode(it) }
        val names = dependencyModel.dependencyNames()

        dependencyModel.implementations.firstOrNull { it.isMethod }?.let {
            return builder.addStatement("singleton = \$T.\$N(\$L)", it.module, it.name, names)
                    .addStatement("return singleton")
                    .build()
        }

        builder.addStatement("singleton = new \$T(\$L)", dependencyModel.typeElement, names)

        val postInitialization = IProcessor.postInitializationMethod(dependencyModel.typeElement)
        if (postInitialization != null) {
            builder.addStatement("singleton.\$N()", postInitialization.simpleName)
        }

        builder.addStatement("return singleton")

        return builder.build()
    }


    private fun generateClearMethod(): MethodSpec {
        val cleanUp = IProcessor.elementUtils.getTypeElement(Cleanable::class.java.canonicalName)

        val builder = MethodSpec.methodBuilder("onCleared")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)

        if (IProcessor.types.isSubtype(dependencyModel.typeElement.asType(), cleanUp.asType())) {
            builder.addStatement("if (singleton != null) singleton.onCleared()")
        }

        builder.addStatement("singleton = null")

        return builder.build()
    }

}