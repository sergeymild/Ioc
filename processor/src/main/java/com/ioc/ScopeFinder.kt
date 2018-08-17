package com.ioc

import com.ioc.common.asElement
import com.ioc.common.isHasAnnotation
import javax.inject.Scope
import javax.inject.Singleton
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.util.ElementFilter

/**
 * Created by sergeygolishnikov on 04/11/2017.
 */
object ScopeFinder {

    fun isKotlinAnnotationsMethod(element: Element): Boolean {
        return element.simpleName.toString().contains("\$annotations")
    }

    fun getScope(element: Element): String? {
        if (element.enclosingElement is TypeElement) {
            val method = ElementFilter.methodsIn(element.enclosingElement.enclosedElements)
                    .firstOrNull { it.simpleName.toString().startsWith("${element.simpleName}\$annotations") }
            if (method != null) {
                return getScope(method)
            }
        }


        return element.annotationMirrors
                .firstOrNull(this::isHasScope)
                ?.annotationType?.asElement()?.simpleName?.toString()
    }


    fun getRootScope(element: Element): String {
        val scopeAnnotation = element.getAnnotation(ScopeRoot::class.java)
        try {
            //scopeAnnotation.value
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror.asElement().simpleName.toString()
        }
        return ROOT_SCOPE
    }

    private fun isHasScope(annotationMirror: AnnotationMirror) = annotationMirror.asElement {
        if (asType().toString() == Singleton::class.java.canonicalName) return@asElement false
        return@asElement isHasAnnotation(Scope::class.java)
    }
}