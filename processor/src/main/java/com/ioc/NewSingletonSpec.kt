package com.ioc

import com.ioc.common.asLazyType
import com.ioc.common.capitalize
import com.ioc.common.keepAnnotation
import com.squareup.javapoet.*
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by sergeygolishnikov on 28/11/2017.
 */
interface SingletonWrapper {
    val dependency: Element
    val originalType: TypeElement
    val packageName: String
    var dependencies: List<DependencyModel>
    var methodProvider: ModuleMethodProvider?
    val originalClassName: TypeName
}

class NewSingletonSpec(private val dependency: SingletonWrapper) {

    @Throws(Throwable::class)
    fun inject(): TypeSpec {

        val singletonName = "${dependency.originalType.simpleName.capitalize()}Singleton"
        return TypeSpec.classBuilder(singletonName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(dependency.originalType.asLazyType())
            .addAnnotation(keepAnnotation)
            .addMethod(getInstanceMethod(singletonName))
            .addMethod(initializeMethod())
            .addField(FieldSpec.builder(ClassName.bestGuess(singletonName), "instance", Modifier.PRIVATE, Modifier.STATIC).build())
            .build()
    }

    private fun getInstanceMethod(singletonName: String): MethodSpec {
        return MethodSpec.methodBuilder("getInstance")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
            .returns(ClassName.bestGuess(singletonName))
            .addStatement("if (instance == null) instance = new \$N()", singletonName)
            .addStatement("return instance")
            .build()
    }

    private fun initializeMethod(): MethodSpec {
        val builder = MethodSpec.methodBuilder("initialize")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
            .returns(dependency.originalClassName)

        val metadata = InjectMethodMetadata(dependency as DependencyModel, TargetType(dependency.originalType))
        val code = DependencyTree.get(dependency.dependencies, metadata)
        builder.addCode(code)

        applyIsLoadIfNeed(dependency.dependencies, null)
        val names = dependency.dependencyNames(metadata)

        dependency.methodProvider?.let {
            return builder.addStatement("return \$T.\$N(\$L)", it.module, it.name, names).build()
        }
        builder.addStatement("return new \$T(\$L)", dependency.originalClassName, names)
        return builder.build()
    }

}