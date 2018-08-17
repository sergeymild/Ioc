package com.ioc.common

import javax.lang.model.element.Element


class ParensSet: HashSet<Element>() {

    override fun add(element: Element): Boolean {
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.asType().toString() == element.asType().toString()) return false
        }
        return super.add(element)
    }


    fun containsAny(elements: Collection<Element>): Boolean {
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next().asType().toString()

            for (element in elements) {
                if (element.asType().toString() == next) return true
            }
        }
        return false
    }

    override fun toString() : String {
        return joinToString(prefix = "(", postfix = ")") { it.asType().toString() }
    }
}