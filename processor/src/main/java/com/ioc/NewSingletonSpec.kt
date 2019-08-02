package com.ioc

import com.ioc.common.asLazyType
import com.ioc.common.capitalize
import com.ioc.common.keepAnnotation
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by sergeygolishnikov on 28/11/2017.
 */
interface SingletonWrapper {
    val typeElement: TypeElement
    val className: ClassName
    val packageName: String
    var dependencies: List<DependencyModel>
    var implementations: List<DependencyProvider>
    var name: String
    fun originalClassName(): TypeName
}

class NewSingletonSpec(private val dependencyModel: SingletonWrapper) {

    @Throws(Throwable::class)
    fun inject(): TypeSpec {

        val singletonName = "${dependencyModel.typeElement.simpleName.capitalize()}Singleton"
        return TypeSpec.classBuilder(singletonName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(dependencyModel.typeElement.asLazyType())
            .addAnnotation(keepAnnotation)
            .addMethod(getterMethod(singletonName))
            .addMethod(generateMethod())
            .addField(FieldSpec.builder(ClassName.bestGuess(singletonName), "instance", Modifier.PRIVATE, Modifier.STATIC).build())
            .build()
    }

    private fun getterMethod(singletonName: String): MethodSpec {
        return MethodSpec.methodBuilder("getInstance")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .returns(ClassName.bestGuess(singletonName))
            .addStatement("if (instance == null) instance = new \$N()", singletonName)
            .addStatement("return instance")
            .build()
    }

    private fun generateMethod(): MethodSpec {
        val builder = MethodSpec.methodBuilder("initialize")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
            .returns(dependencyModel.className)

        val code = DependencyTree.get(dependencyModel.dependencies)
        builder.addCode(code)

        applyIsLoadIfNeed(dependencyModel.dependencies, null)
        val names = dependencyModel.dependencyNames()

        dependencyModel.implementations.firstOrNull { it.isMethod }?.let {
            return builder.addStatement("return \$T.\$N(\$L)", it.module, it.name, names).build()
        }
        builder.addStatement("return new \$T(\$L)", dependencyModel.typeElement, names)
        return builder.build()
    }

}