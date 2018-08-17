package com.ioc

import com.ioc.common.asElement
import com.ioc.common.isHasAnnotation
import javax.inject.Named
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter

/**
 * Created by sergeygolishnikov on 04/11/2017.
 */
class QualifierFinder {

    fun hasNamed(named: String?, provider: Element) : Boolean {
        return named == getQualifier(provider)
//        return named == (getQualifier(provider) ?: "")
    }

    fun getQualifier(element: Element): String? {

        if (element.enclosingElement is TypeElement) {
            val method = ElementFilter.methodsIn(element.enclosingElement.enclosedElements)
                    .firstOrNull { it.simpleName.toString().startsWith("${element.simpleName}\$annotations") }
            if (method != null) {
                return getQualifier(method)
            }
        }
        try {
            element.getAnnotation(Named::class.java)?.let { return it.value }
        } catch (e: Throwable) {
            return null
        }

        val qualifier: AnnotationMirror? = element.annotationMirrors.firstOrNull(this::isHasQualifier)
        if (qualifier == null) return null
        if (qualifier.annotationType == null) return null
        val annotationType = qualifier.annotationType
        if (annotationType !is DeclaredType) return null
        val declared = annotationType as DeclaredType
        return declared.asElement().simpleName.toString()
    }

    private fun isHasQualifier(annotationMirror: AnnotationMirror) = annotationMirror.asElement {
        return@asElement isHasAnnotation(Qualifier::class.java)
    }
}