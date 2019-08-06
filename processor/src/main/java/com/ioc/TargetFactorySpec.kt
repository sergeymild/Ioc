package com.ioc

import com.ioc.common.hashMapType
import com.ioc.common.keepAnnotation
import com.ioc.common.targetInjectionTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

object TargetFactorySpec {
    fun createSpec(targets: Collection<TargetType>): TypeSpec {
        val typeSpec = TypeSpec.classBuilder("TargetFactoryImplementation")
            .addModifiers(Modifier.FINAL)
            .superclass(ClassName.get(TargetFactory::class.java))
            .addAnnotation(keepAnnotation)

        val staticBlock = CodeBlock.builder()
        var count = 0
        for (target in targets) {
            count += 1


            staticBlock.addStatement("map.put(\$T.class, \$T.class)",
                target.className, targetInjectionTypeName(target))
        }


        typeSpec.addStaticBlock(CodeBlock.builder()
            .addStatement("map = new \$T<>(\$L)", hashMapType, count)
            .addStatement("cachedInjectMethods = new \$T<>(\$L)", hashMapType, count)
            .add(staticBlock.build())
            .build())

        return typeSpec.build()
    }

}