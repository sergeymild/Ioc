package com.ioc

import com.ioc.common.asTypeElement
import com.ioc.common.isEqualTo
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

/**
 * Created by sergeygolishnikov on 27/11/2017.
 */

object TargetChecker {

    fun isSubtype(target: TargetType, dependency: Element): Boolean {
        if (target.element.isEqualTo(dependency)) return true

        return target.supertypes.any { dependency.isEqualTo(it) }
    }

    fun isFromTarget(target: TargetType, dependency: TypeMirror): ExecutableElement? {
        return target.methods.firstOrNull { it.returnType.asTypeElement().isEqualTo(dependency) }

    }
}