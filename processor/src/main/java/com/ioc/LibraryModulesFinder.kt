package com.ioc

import com.ioc.common.addTo
import com.ioc.common.asTypeElement
import com.ioc.common.isHasAnnotation
import com.ioc.common.isMethod
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.MirroredTypesException

fun findLibraryModules(annotation: LibraryModules?, alreadyRead: MutableSet<String>) {
    annotation ?: return

    try {
        annotation.value
    } catch (e: MirroredTypesException) {
        e.typeMirrors
                .filterNot { alreadyRead.contains(it.toString()) }
                .map { it.asTypeElement() }
                .onEach { alreadyRead.add(it.toString()) }
                .onEach { findLibraryModules(it.getAnnotation(LibraryModules::class.java), alreadyRead) }
                .flatMap { it.enclosedElements }
                .filter { it.isMethod() }
                .filter { it.isHasAnnotation(Dependency::class.java) }
                .filter { it.modifiers.contains(Modifier.STATIC) || it.modifiers.contains(Modifier.ABSTRACT) }
                .map { it as ExecutableElement }
                .addTo(IProcessor.methodsWithDependencyAnnotation)

    }

}