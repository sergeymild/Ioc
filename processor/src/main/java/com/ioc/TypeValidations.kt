package com.ioc

import com.ioc.common.isCanHaveViewModel
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind


@Throws(ProcessorException::class)
fun validateObserverMethod(method: ExecutableElement) {
    if (method.modifiers.contains(Modifier.PRIVATE)) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must be public.").setElement(method.enclosingElement)
    }

    if (method.parameters.size > 1) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must contains only one parameter.").setElement(method.enclosingElement)
    }

    if (method.returnType.kind != TypeKind.VOID) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must not contains return type.").setElement(method.enclosingElement)
    }
}

@Throws(ProcessorException::class)
fun validateTargetForLiveDataObserver(typeElement: TypeElement) {
    if (!typeElement.isCanHaveViewModel()) {
        throw ProcessorException("@DataObserver methods may be placed only in Activity or Fragment but was found in $typeElement.").setElement(typeElement)
    }
}