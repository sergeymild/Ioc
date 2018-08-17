package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.*
import javax.lang.model.element.Modifier
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */

fun scopeName(dependency: DependencyModel): String {
    return "${dependency.typeElement.simpleName.capitalize()}${dependency.scoped?.replace("Scope", "")}Scope"
}

fun scopeRootType(target: TargetType): ClassName? {
    return ClassName.get(target.className.packageName(), scopeRootName(target))
}

fun scopeRootName(target: TargetType): String {
    return "${target.rootScope.replace("Scope", "")}ScopeRoot"
}

fun scopeTypeName(dependency: DependencyModel): ClassName {
    return ClassName.get(dependency.packageName, scopeName(dependency))
}

class ScopeSpec(private val dependency: DependencyModel,
                private val target: TargetType,
                private val typeUtils: Types) {

    companion object {
        fun initializeScopeSpec(target: TargetType, scopes: List<DependencyModel>, typeUtils: Types): TypeSpec {
            val typeSpec = TypeSpec.classBuilder(scopeRootType(target))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(keepAnnotation)
                    .addMethod(createScope(target, scopes))

            for (model in scopes) {
                if (model.scoped != target.rootScope || (model.asTarget && model.implementations.isEmpty())) continue
                typeSpec.addMethod(cache(model.cacheMethodName, target, model, typeUtils))
            }

            return typeSpec.build()
        }

        private fun createScope(target: TargetType, scopes: List<DependencyModel>): MethodSpec {
            val builder = MethodSpec.methodBuilder("createScope")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .addParameter(scopeHolderParameter(target))
                    .addAnnotation(keepAnnotation)

            for (model in scopes) {
                if (model.scoped != target.rootScope || (model.asTarget && model.implementations.isEmpty())) continue
                builder.addStatement("\$N(target)", model.cacheMethodName)
            }

            return builder.build()
        }


        private fun cache(methodName: String, target: TargetType, dependency: DependencyModel, typeUtils: Types): MethodSpec {
            val builder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addParameter(scopeHolderParameter(target))
                    .addAnnotation(keepAnnotation)

            // Get dependency from cache scope
            builder.addStatement("\$T \$N = \$T.<\$T>get(\$S, \$T.class)",
                    dependency.dependency,
                    dependency.scopedName,
                    scopeFactoryType,
                    dependency.originalClassName(),
                    dependency.scoped,
                    dependency.originalClassName())

            // if dependency is null create and cache new one
            builder.beginControlFlow("if (\$N == null)", dependency.scopedName)
            DependencyTree.get(dependency.depencencies, typeUtils, target).also { builder.addCode(it) }

            val method = dependency.provideMethod()
            if (method != null) {
                builder.addStatement("\$N = \$T.\$N(\$L)",
                        dependency.scopedName,
                        method.module,
                        method.name,
                        dependency.dependencyNames())
            } else {
                builder.addStatement("\$N = new \$T(\$L)",
                        dependency.scopedName,
                        dependency.typeElement,
                        dependency.dependencyNames())
            }


            builder.addStatement("\$T.cache(target, \$S, \$S, \$N)",
                    scopeFactoryType,
                    target.rootScope,
                    dependency.name,
                    dependency.scopedName)

            builder.endControlFlow()

            return builder.build()
        }
    }

    @Throws(Throwable::class)
    fun inject(): TypeSpec {
        return TypeSpec.classBuilder(scopeName(dependency))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)
                .addField(FieldSpec.builder(TypeName.BOOLEAN, "isInitialized", Modifier.PUBLIC).build())
                .addMethod(cache("cache", target, dependency, typeUtils))
                .addMethod(get())
                .build()
    }



    private fun get(): MethodSpec {
        val builder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(keepAnnotation)
                .addAnnotation(nonNullAnnotation)
                .returns(dependency.className)

        builder.addStatement("\$T \$N = \$T.<\$T>get(\$S, \$T.class)",
                dependency.dependency,
                dependency.generatedName,
                ClassName.get(ScopeFactory::class.java),
                dependency.originalClassName(),
                dependency.scoped,
                dependency.originalClassName())

        builder.addStatement("return \$N", dependency.generatedName)

        return builder.build()
    }

}