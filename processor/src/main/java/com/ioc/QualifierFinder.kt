package com.ioc

import com.ioc.common.asElement
import com.ioc.common.isHasAnnotation
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element

/**
 * Created by sergeygolishnikov on 04/11/2017.
 */
class QualifierFinder {

    fun hasNamed(named: String?, provider: Element) : Boolean {
        return named == getQualifier(provider)
    }

    fun getQualifier(element: Element?): String? {
        element ?: return null


        try {
            element.getAnnotation(Qualifier::class.java)?.let { return it.value }
        } catch (e: Throwable) {
            return null
        }

        val qualifier = element.annotationMirrors.firstOrNull(this::isHasQualifier) ?: return null
        if (qualifier.annotationType == null) return null
        return qualifier.annotationType?.asElement()?.simpleName?.toString()
    }

    private fun isHasQualifier(annotationMirror: AnnotationMirror) = annotationMirror.asElement {
        return@asElement isHasAnnotation(Qualifier::class.java)
    }
}