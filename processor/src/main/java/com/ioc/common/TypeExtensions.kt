package com.ioc.common

import com.ioc.*
import com.squareup.javapoet.ClassName
import java.lang.ref.WeakReference
import java.util.LinkedHashSet
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.ElementScanner8
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

fun TypeElement.asClassName(): ClassName {
    return ClassName.get(this)
}

@Throws(ProcessorException::class)
fun Element.asTypeElement(): TypeElement {
    if (!isSupportedType()) throw ProcessorException("Unsupported type $simpleName in $enclosingElement").setElement(this)
    if (this is TypeElement) return this
    try {
        return this.asType().asTypeElement()
    } catch (e: Throwable) {
        throw ProcessorException("Can't convert ${this.simpleName}: ${this.asType()} to TypeElement").setElement(this)
    }
}

fun Element.methods(predicate: (ExecutableElement) -> Boolean): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements).filter(predicate)
}


fun Element.isPrivate(): Boolean {
    return modifiers.contains(Modifier.PRIVATE)
}

fun Element.isPublic(): Boolean {
    return modifiers.contains(Modifier.PUBLIC)
}

fun Element.getGenericFirstType(): Element {
    if (!isSupportedType()) throw ProcessorException("Unsupported type $simpleName").setElement(this)
    return asType().getGenericFirstType()
}

fun <A : Annotation> Element.isHasAnnotation(annotation: Class<A>): Boolean {
    return getAnnotation(annotation) != null
}

fun <A : Annotation> Element.isNotHasAnnotation(annotation: Class<A>): Boolean {
    return getAnnotation(annotation) == null
}


fun Element.getPackage(): PackageElement {
    return MoreElements.getPackage(this)
}

val targetDependencies = mutableMapOf<String, MutableSet<Element>>()
val rootTypeElements = mutableListOf<TypeElement>()

class AnnotationSetScanner(
    private val processingEnvironment: ProcessingEnvironment,
    elements: MutableSet<Element>) : ElementScanner8<MutableSet<Element>, TypeElement>(elements) {
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

fun RoundEnvironment.rootElementsWithInjectedDependencies(): List<TypeElement> {
    targetDependencies.clear()
    rootTypeElements.clear()

    val injectedElements = getElementsAnnotatedWith(Inject::class.java)

    for (dependencyElement in injectedElements) {
        val enclosingElement = dependencyElement.enclosingElement

        // if first time meet class element
        if (!targetDependencies.containsKey(enclosingElement.asType().toString())) {
            val typeElement = enclosingElement.asTypeElement()
            rootTypeElements.add(typeElement)
        }

        val dependencies = targetDependencies.getOrPut(enclosingElement.asType().toString()) { mutableSetOf() }
        dependencies.add(dependencyElement)
    }

    return rootTypeElements
}

fun RoundEnvironment.findDependenciesInParents(processingEnv: ProcessingEnvironment) {
    val uniqueRootTypeElements = mutableSetOf<String>()

    val injectAnnotationType = processingEnv.elementUtils.getTypeElement(Inject::class.java.canonicalName)
    for (childElement in getElementsAnnotatedWith(InjectParentDependencies::class.java)) {
        val typeElement = childElement.asTypeElement()
        var superclass = typeElement.superclass
        var isDependenciesFound = false
        while (!superclass.isNotValid()) {
            if (uniqueRootTypeElements.contains(superclass.toString())) continue
            val superclassTypeElement = superclass.asTypeElement()

            val injectElements = mutableSetOf<Element>()
            val scanner = AnnotationSetScanner(processingEnv, injectElements)
            val found = scanner.scan(superclassTypeElement, injectAnnotationType)

            val dependencies = targetDependencies.getOrPut(superclass.toString()) { mutableSetOf() }
            dependencies.addAll(found)
            if (dependencies.isNotEmpty()) {
                rootTypeElements.add(superclassTypeElement)
                isDependenciesFound = true
            }
            superclass = superclassTypeElement.superclass
            if (superclass.isNotValid()) break
        }

        if (isDependenciesFound && !rootTypeElements.contains(typeElement)) {
            rootTypeElements.add(typeElement)
            targetDependencies.getOrPut(typeElement.asType().toString()) { mutableSetOf() }
        }
    }
}

fun mapToTargetWithDependencies(dependencyResolver: DependencyResolver): Map<TargetType, MutableList<DependencyModel>> {
    val targetsWithDependencies = mutableMapOf<TargetType, MutableList<DependencyModel>>()

    for (targetTypeElement in rootTypeElements) {
        val targetType = IProcessor.createTarget(targetTypeElement, IProcessor.dependencyFinder)

        val dependencies = targetsWithDependencies.getOrPut(targetType) { mutableListOf() }

        val injectElements = targetDependencies.getValue(targetTypeElement.asType().toString())
        for (injectElement in injectElements) {
            if (injectElement.kind == ElementKind.CONSTRUCTOR) continue
            val resolved = dependencyResolver.resolveDependency(injectElement, target = targetType, skipCheckFromTarget = true)
            dependencies.add(resolved)
        }
    }

    return targetsWithDependencies
}

fun methodsWithDependencyAnnotation(): List<ExecutableElement> {
    return IProcessor.methodsWithDependencyAnnotation
}

// Only get all classes with annotation @Dependency
fun classesWithDependencyAnnotation(): List<Element> {
    return IProcessor.classesWithDependencyAnnotation
}

// Only get all classes with annotation @Dependency
fun abstractMethodsWithDependencyAnnotations(): List<ExecutableElement> {
    return IProcessor
        .methodsWithDependencyAnnotation
        .filter { it.modifiers.contains(Modifier.ABSTRACT) }
}

fun Element.isNotMethodAndInterface(): Boolean {
    return kind != ElementKind.METHOD && kind != ElementKind.INTERFACE
}

fun TypeMirror.isNotValid(): Boolean {
    return toString() == Object::class.java.canonicalName
        || kind == TypeKind.NONE
        || kind == TypeKind.PACKAGE
        || kind == TypeKind.NULL
}

fun Element.isEqualTo(other: Element): Boolean {
    return this.asType().toString() == other.asType().toString()
}

fun TypeMirror.isEqualTo(other: Element): Boolean {
    return toString() == other.asType().toString()
}

fun Element?.isEqualTo(other: TypeMirror): Boolean {
    this ?: return false
    return this.asType().toString() == other.toString()
}

fun DeclaredType.getGenericFirstType(): Element {
    return typeArguments[0].asElement()
}

fun TypeMirror.getGenericFirstType(): Element {
    return asDeclared().getGenericFirstType()
}

fun TypeMirror.asElement(): Element {
    return MoreTypes.asElement(this)
}

fun TypeMirror.asTypeElement(): TypeElement {
    return MoreTypes.asTypeElement(this)
}

fun TypeMirror.safeTypeElement(): TypeElement? {
    try {
        return MoreTypes.asTypeElement(this)
    } catch (e: IllegalArgumentException) {
        return null
    }
}

fun List<TypeMirror>.methodsWithTargetDependency(): List<ExecutableElement> {
    val methods = mutableListOf<ExecutableElement>()

    for (supertype in this) {
        for (method in ElementFilter.methodsIn(supertype.asTypeElement().enclosedElements)) {
            if (method.isNotHasAnnotation(TargetDependency::class.java)) continue
            if (method.modifiers.contains(Modifier.PRIVATE)) continue
            if (method.parameters.isNotEmpty()) continue
            val element = method.returnType.safeTypeElement() ?: continue
            if (!element.isSupportedType()) continue
            methods.add(method)
        }

    }

    return methods
}

fun TypeMirror.asDeclared(): DeclaredType {
    return MoreTypes.asDeclared(this)
}

fun TypeMirror.typeArguments(): List<TypeMirror> {
    if (this !is DeclaredType) return emptyList()
    return MoreTypes.asDeclared(this).typeArguments
}


inline fun <R> AnnotationMirror.asElement(block: Element.() -> R): R {
    return block(annotationType.asElement())
}

inline fun <reified T> T?.orElse(closure: () -> Unit): T {
    if (this == null) closure()
    return this!!
}

fun Name.capitalize(): String {
    return toString().capitalize()
}

fun Element.isHasArgumentsConstructor(): Boolean =
    ElementFilter.constructorsIn(asTypeElement().enclosedElements)
        .any { !it.parameters.isEmpty() || it.isHasAnnotation(Inject::class.java) }

fun Element.argumentsConstructor(): ExecutableElement? =
    ElementFilter.constructorsIn(asTypeElement().enclosedElements)
        .firstOrNull { !it.parameters.isEmpty() || it.isHasAnnotation(Inject::class.java) }

fun Element.injectionFields(): List<Element> =
    ElementFilter.fieldsIn(asTypeElement().enclosedElements)
        .filter { it.isHasAnnotation(Inject::class.java) }


fun Element.isWeakDependency(types: Types): Boolean {
    return types.erasure(asType()).toString() == WeakReference::class.java.canonicalName
}

fun Element.isProvideDependency(types: Types): Boolean {
    return types.erasure(asType()).toString() == Provider::class.java.canonicalName
}

fun Element.isViewModel(): Boolean {
    if (asType().kind.isPrimitive) return false

    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.kind == TypeKind.NONE) break
        if (superType.toString() == "android.arch.lifecycle.ViewModel") return true
        superType = superType.asTypeElement().superclass
    }
    return false
}

fun Element.isCanHaveViewModel(): Boolean {
    if (asType().kind.isPrimitive) return false

    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.kind == TypeKind.NONE) break
        if (superType.toString() == "android.support.v4.app.Fragment") return true
        if (superType.toString() == "android.support.v4.app.FragmentActivity") return true
        superType = superType.asTypeElement().superclass
    }
    return false
}

fun Element.isLazy(types: Types): Boolean {
    return types.erasure(asType()).toString() == com.ioc.Lazy::class.java.canonicalName
}

fun Element.isMethod(): Boolean {
    return kind == ElementKind.METHOD
}

fun Element.isInterface(): Boolean {
    return kind == ElementKind.INTERFACE
}

fun <T> List<T>.addTo(other: MutableList<T>) {
    other.addAll(this)
}

class SetterAndGetter(val setter: ExecutableElement, val getter: Element)

@Throws(ProcessorException::class)
fun findDependencySetter(element: Element): ExecutableElement? {
    return element.enclosingElement.methods {
        it.isPublic() && it.parameters.size == 1 && it.parameters[0].isEqualTo(element)
    }.firstOrNull()
}

@Throws(ProcessorException::class)
fun findDependencyGetter(element: Element): Element {
    if (element.isPublic()) return element
    return element.enclosingElement.methods {
        it.isPublic() && it.parameters.isEmpty() && it.returnType.toString() == element.asType().toString()
    }.firstOrNull() ?: throw ProcessorException("@Inject annotation placed on field `${element.simpleName}` in `${element.enclosingElement.simpleName}` with private access and which does't have public getter method.").setElement(element)
}

fun Element.toGetterName(): String = if (this is ExecutableElement) "$simpleName()" else simpleName.toString()

fun findSetterAndGetterMethods(element: Element): SetterAndGetter {
    val setterMethod = findDependencySetter(element).orElse {
        throw ProcessorException("@Inject annotation placed on field `${element.simpleName}` in `${element.enclosingElement.simpleName}` with private access and which does't have public setter method.").setElement(element)
    }
    val getterMethod = findDependencyGetter(element)
    return SetterAndGetter(setterMethod, getterMethod)
}