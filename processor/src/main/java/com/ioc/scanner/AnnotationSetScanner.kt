package com.ioc.scanner

import com.ioc.common.isConstructor
import com.ioc.common.isMethod
import java.util.LinkedHashSet
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementScanner8

class AnnotationSetScanner(
    private val processingEnvironment: ProcessingEnvironment,
    elements: MutableSet<Element>,
    private val skipMethods: Boolean = false) : ElementScanner8<MutableSet<Element>, TypeElement>(elements) {
    private var annotatedElements: MutableSet<Element> = LinkedHashSet()

    override fun visitType(var1: TypeElement, var2: TypeElement): MutableSet<Element> {
        this.scan(var1.typeParameters, var2)
        return super.visitType(var1, var2)
    }

    override fun visitExecutable(var1: ExecutableElement, var2: TypeElement): MutableSet<Element> {
        this.scan(var1.typeParameters, var2)
        return super.visitExecutable(var1, var2)
    }

    override fun scan(var1: Element, var2: TypeElement): MutableSet<Element> {
        if (skipMethods && (var1.isConstructor() || var1.isMethod())) return mutableSetOf()
        val var3 = processingEnvironment.elementUtils.getAllAnnotationMirrors(var1)
        val var4 = var3.iterator()

        while (var4.hasNext()) {
            val var5 = var4.next() as AnnotationMirror
            if (var2 == var5.annotationType.asElement()) {
                this.annotatedElements.add(var1)
            }
        }

        var1.accept(this, var2)
        return this.annotatedElements
    }
}
