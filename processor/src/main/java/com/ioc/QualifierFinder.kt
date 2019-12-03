package com.ioc

import com.ioc.common.asElement
import com.ioc.common.asMethod
import com.ioc.common.isHasAnnotation
import com.ioc.common.isMethod
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeKind

/**
 * Created by sergeygolishnikov on 04/11/2017.
 */
class QualifierFinder {

    fun hasNamed(named: String?, provider: Element) : Boolean {
        return named == getQualifier(provider)
    }

    fun getModuleMethodQualifier(element: Element): String {
        var qualifier: String? = null
        var typeString = element.asTypeString()
        getQualifier(element)?.let { qualifier = it }
        qualifier?.let { return "${it}_$typeString" }

        if (element.isMethod()) {
            val method = element.asMethod()
            val returnType = method.returnType
            if (returnType.kind == TypeKind.DECLARED) {
                typeString = returnType.toString()
                qualifier = getQualifier(returnType.asElement())
            }
        }
        qualifier?.let { return "${it}_$typeString" }
        return element.asTypeString()
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