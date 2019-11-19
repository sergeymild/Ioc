package com.ioc

import com.ioc.common.asTypeElement
import com.ioc.common.isNotValid
import com.ioc.common.keepAnnotation
import com.ioc.common.singletonTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Modifier
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
                if (type.toString() == Cleanable::class.java.canonicalName) continue

                count += 1
                val typeElement = type.asTypeElement()
                staticBlock.addStatement("map.put(\$T.class, \$T.class)",
                    type, singletonTypeName(singleton))

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