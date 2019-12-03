package com.ioc

import com.ioc.common.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.collections.HashMap

object SingletonFactorySpec {
    fun createSpec(singletons: Collection<DependencyModel>): TypeSpec {
        val typeSpec = TypeSpec.classBuilder("SingletonsFactoryImplementation")
            .addModifiers(Modifier.FINAL)
            .superclass(ClassName.get(SingletonFactory::class.java))
            .addAnnotation(keepAnnotation)

        val staticBlock = CodeBlock.builder()
        val queue = LinkedList<TypeMirror>()
        var count = 0
        for (singleton in singletons) {
            queue.add(singleton.originalType.asType())
            while (queue.isNotEmpty()) {
                val type = queue.pop()
                if (type.isNotValid()) continue
                val typeElement = type.asTypeElement()
                if (type.isValidMapKey()) {
                    count += 1
                    staticBlock.addStatement("map.put(\$T.class, \$T.class)",
                        type, singletonTypeName(singleton))
                }

                queue.addAll(typeElement.interfaces)
                queue.add(typeElement.superclass)
            }

            queue.clear()

        }


        typeSpec.addStaticBlock(CodeBlock.builder()
            .addStatement("map = new \$T<>(\$L)", ClassName.get(HashMap::class.java), count)
            .addStatement("cachedSingletons = new \$T<>(\$L)", ClassName.get(HashMap::class.java), singletons.size)
            .add(staticBlock.build())
            .build())

        return typeSpec.build()
    }
}