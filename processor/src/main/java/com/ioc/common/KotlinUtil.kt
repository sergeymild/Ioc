package com.ioc.common

import javax.lang.model.element.TypeElement
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata

object KotlinUtil {
    internal fun kmClassOf(typeElement: TypeElement?): KmClass? {
        typeElement ?: return null
        val metadataAnnotation = typeElement.getAnnotation(Metadata::class.java) ?: return null
        val header = KotlinClassHeader(
            metadataAnnotation.kind,
            metadataAnnotation.metadataVersion,
            metadataAnnotation.bytecodeVersion,
            metadataAnnotation.data1,
            metadataAnnotation.data2,
            metadataAnnotation.extraString,
            metadataAnnotation.packageName,
            metadataAnnotation.extraInt
        )
        val metadata = KotlinClassMetadata.read(header)
            ?: // Should only happen on Kotlin <1.1
            return null
        return if (metadata is KotlinClassMetadata.Class) {
            metadata.toKmClass()
        } else {
            // Unsupported
            null
        }
    }
}


inline fun <T> Collection<T>.toMutableMap(consumer: (T) -> String): MutableMap<String, T> {
    val resultMap = mutableMapOf<String, T>()
    for (t in this) resultMap[consumer(t)] = t
    return resultMap
}