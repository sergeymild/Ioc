package com.ioc.common

import com.ioc.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import java.lang.ref.WeakReference
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Types

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */
val scopeFactoryType = ClassName.get(ScopeFactory::class.java)

fun TypeElement.asClassName(): ClassName {
    return ClassName.get(this)
}

fun scopeHolderParameter(target: TargetType): ParameterSpec {
    return ParameterSpec
            .builder(target.className, "target", Modifier.FINAL)
            .addAnnotation(nonNullAnnotation)
            .build()
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

fun Element?.collectionMethods(types: Types, container: MutableList<ExecutableElement>) {
    this ?: return
    container.addAll(ElementFilter.methodsIn(enclosedElements))
    types.directSupertypes(asType()).forEach {
        it.asTypeElement().collectionMethods(types, container)
    }
}

fun Element.methods(predicate: (ExecutableElement) -> Boolean): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements).filter(predicate)
}

fun Element.fields(): List<Element> {
    return ElementFilter.fieldsIn(enclosedElements)
}

fun Element.isType(): Boolean {
    return MoreElements.isType(this)
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

fun <A: Annotation> Element.isHasAnnotation(annotation: Class<A>): Boolean {
    return getAnnotation(annotation) != null
}

fun <A: Annotation> Element.isNotHasAnnotation(annotation: Class<A>): Boolean {
    return getAnnotation(annotation) == null
}

fun Element.asExecutable(): ExecutableElement {
    return MoreElements.asExecutable(this)
}

fun Element.isExecutable(): Boolean {
    return MoreElements.isExecutable(this)
}

fun Element.asPackage(): PackageElement {
    return MoreElements.asPackage(this)
}

fun Element.getPackage(): PackageElement {
    return MoreElements.getPackage(this)
}

fun RoundEnvironment.fieldsWithInjectAnnotation(): List<Element> {
    return getElementsAnnotatedWith(Inject::class.java)
            .filter { it.kind != ElementKind.CONSTRUCTOR }
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
    return toString() == Object::class.java.canonicalName || kind == TypeKind.NONE || kind == TypeKind.PACKAGE || kind == TypeKind.NULL
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

fun Element?.isNotEqualTo(other: TypeMirror): Boolean {
    this ?: return true
    return this.asType().toString() != other.toString()
}

fun Element.asDeclared(): DeclaredType {
    return asType().asDeclared()
}

fun DeclaredType.enclosingType(): TypeMirror {
    return MoreTypes.enclosingType(this)
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

fun TypeMirror.asExecutable(): ExecutableType {
    return MoreTypes.asExecutable(this)
}

fun TypeMirror.isType(): Boolean {
    return MoreTypes.isType(this)
}

fun TypeMirror.isTypeOf(clazz: Class<*> ): Boolean {
    return MoreTypes.isTypeOf(clazz, this)
}

fun AnnotationMirror.asElement() : Element {
    return annotationType.asElement()
}

inline fun <R> AnnotationMirror.asElement(block: Element.() -> R): R {
    return block(annotationType.asElement())
}

inline fun <reified T> T?.orElse(closure: () -> Unit) : T {
    if (this == null) closure()
    return this!!
}


fun Name.decapitalize() : String {
    return toString().decapitalize()
}

fun Name.capitalize() : String {
    return toString().capitalize()
}

fun Element.isHasArgumentsConstructor() : Boolean =
        ElementFilter.constructorsIn(asTypeElement().enclosedElements)
                .any { !it.parameters.isEmpty() || it.isHasAnnotation(Inject::class.java) }

fun Element.argumentsConstructor() : ExecutableElement? =
        ElementFilter.constructorsIn(asTypeElement().enclosedElements)
                .firstOrNull { !it.parameters.isEmpty() || it.isHasAnnotation(Inject::class.java) }

fun Element.injectionFields() : List<Element> =
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

fun Element.isRootScope(): Boolean {
    return isHasAnnotation(ScopeRoot::class.java)
}

fun Element.isChildScope(): Boolean {
    return isHasAnnotation(ScopeChild::class.java)
}

fun Element.isParameter(): Boolean {
    return kind == ElementKind.PARAMETER
}

fun Element.isMethod(): Boolean {
    return kind == ElementKind.METHOD
}

fun Element.isMethodWithReturnType(): Boolean {
    return kind == ElementKind.METHOD
            && (this.asExecutable().returnType.kind != TypeKind.VOID)
            && (!this.asExecutable().returnType.kind.isPrimitive)
}

fun Element.isInterface(): Boolean {
    return kind == ElementKind.INTERFACE
}

fun <T>  List<T>.addTo(other: MutableList<T>) {
    other.addAll(this)
}