package com.ioc.common

import com.ioc.IProcessor
import com.squareup.javapoet.*
import kotlinx.metadata.Flag
import java.util.concurrent.TimeUnit
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic


/**
 * Created by sergeygolishnikov on 20/11/2017.
 */

val emptyCodBlock = CodeBlock.builder().build()

fun Element.asTypeName(): TypeName {
    return ClassName.get(asType())
}

fun CodeBlock.add(codeBlock: CodeBlock): CodeBlock {
    return toBuilder().add(codeBlock).build()
}

fun CodeBlock.add(block: CodeBlock.Builder): CodeBlock.Builder {
    return block.add(this)
}

fun Element.asLazyType(): TypeName {
    return ParameterizedTypeName.get(iocLazyType, asTypeName())
}

fun Element.asProviderType(): TypeName {
    return ParameterizedTypeName.get(iocProviderType, asTypeName())
}

fun Element.asWeakType(): TypeName {
    return ParameterizedTypeName.get(weakType, asTypeName())
}

fun viewModelFactoryCode(name: CharSequence, code: CodeBlock.Builder): CodeBlock.Builder {
    return CodeBlock.builder().add("\$T \$N = \$L;\n",
        viewModelFactoryType,
        "factory_$name",
        TypeSpec.anonymousClassBuilder("")
            .addSuperinterface(viewModelFactoryType)
            .addMethod(MethodSpec.methodBuilder("create")
                .addAnnotation(nonNullAnnotation)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(javaClassType, "modelClass", Modifier.FINAL).addAnnotation(nonNullAnnotation).build())
                .addTypeVariable(TypeVariableName.get("T", viewModelType))
                .returns(TypeVariableName.get("T"))
                .addCode(code.build())
                .build())
            .build())
}


fun message(message: Any?) {
    IProcessor.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "$message")
}


inline fun measure(message: String, block: () -> Unit) {
    val start = System.currentTimeMillis()
    try {
        block()
    } finally {
        val end = System.currentTimeMillis()
        message("$message: [secs: ${TimeUnit.MILLISECONDS.toSeconds(end - start)}]")
    }
}


fun isModuleKotlinObject(typeElement: TypeElement): Boolean {
    val kmClass = KotlinUtil.kmClassOf(typeElement) ?: return false
    return Flag.Class.IS_OBJECT.invoke(kmClass.flags)
}

fun Element?.isSupportedType(): Boolean {
    this ?: return false
    if (asType().kind == TypeKind.ARRAY) return false
    if (asType().kind == TypeKind.VOID) return false
    if (asType().kind == TypeKind.NONE) return false
    if (asType().kind == TypeKind.NULL) return false
    if (asType().kind == TypeKind.ERROR) return false
    if (asType().kind == TypeKind.TYPEVAR) return false
    if (asType().kind == TypeKind.WILDCARD) return false
    if (asType().kind == TypeKind.PACKAGE) return false
    if (asType().kind == TypeKind.OTHER) return false
    if (asType().kind == TypeKind.UNION) return false
    if (asType().kind == TypeKind.INTERSECTION) return false
    if (asType().kind.isPrimitive) return false
    return true
}