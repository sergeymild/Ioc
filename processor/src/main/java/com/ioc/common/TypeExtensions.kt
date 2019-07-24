package com.ioc.common

import com.ioc.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import java.lang.ref.WeakReference
import java.util.LinkedHashSet
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.ElementScanner8
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

fun Element.methods(predicate: (ExecutableElement) -> Boolean): List<ExecutableElement> {
    return ElementFilter.methodsIn(enclosedElements).filter(predicate)
}

fun Element.fields(): List<Element> {
    return ElementFilter.fieldsIn(enclosedElements)
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

fun Element.asExecutable(): ExecutableElement {
    return MoreElements.asExecutable(this)
}


fun Element.getPackage(): PackageElement {
    return MoreElements.getPackage(this)
}

private val targetDependencies = mutableMapOf<String, MutableSet<Element>>()

class AnnotationSetScanner(
    val processingEnvironment: ProcessingEnvironment,
    elements: MutableSet<Element>): ElementScanner8<MutableSet<Element>, TypeElement>(elements) {
    internal var annotatedElements: MutableSet<Element> = LinkedHashSet()

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

fun RoundEnvironment.rootElementsWithInjectedDependencies(processingEnv: ProcessingEnvironment): List<TypeElement> {
    targetDependencies.clear()
    val rootTypeElements = mutableListOf<TypeElement>()
    val uniqueRootTypeElements = mutableSetOf<String>()

    val injectAnnotationType = processingEnv.elementUtils.getTypeElement(Inject::class.java.canonicalName)
    val injectedElements = getElementsAnnotatedWith(Inject::class.java)
    message("-> injected: $injectedElements")

    for (dependencyElement in injectedElements) {
        val enclosingElement = dependencyElement.enclosingElement

        // if first time meet class element
        if (!targetDependencies.containsKey(enclosingElement.asType().toString())) {
            val typeElement = enclosingElement.asTypeElement()
            rootTypeElements.add(typeElement)
            if (typeElement.isHasAnnotation(ParentDependencies::class.java)) {
                var superclass = typeElement.superclass
                while (!superclass.isNotValid()) {
                    if (uniqueRootTypeElements.contains(superclass.toString())) continue
                    val superclassTypeElement = superclass.asTypeElement()

                    val injectElements = mutableSetOf<Element>()
                    val scanner = AnnotationSetScanner(processingEnv, injectElements)
                    superclassTypeElement.accept(scanner, injectAnnotationType)

                    val dependencies = targetDependencies.getOrPut(superclass.toString()) { mutableSetOf() }
                    dependencies.addAll(injectElements)
                    rootTypeElements.add(superclassTypeElement)
                    superclass = superclassTypeElement.superclass
                    if (superclass.isNotValid()) break
                }
            }
        }

        
        val dependencies = targetDependencies.getOrPut(enclosingElement.asType().toString()) { mutableSetOf() }
        dependencies.add(dependencyElement)
    }

    return rootTypeElements
}

fun mapToTargetWithDependencies(targets: List<TypeElement>, dependencyResolver: DependencyResolver): Map<TargetType, MutableList<DependencyModel>> {
    val targetsWithDependencies = mutableMapOf<TargetType, MutableList<DependencyModel>>()

    message("-> targets: $targets")

    for (targetTypeElement in targets) {
        val targetType = IProcessor.createTarget(targetTypeElement, IProcessor.dependencyFinder)

        val dependencies = targetsWithDependencies.getOrPut(targetType) { mutableListOf() }

        val injectElements = targetDependencies.getValue(targetTypeElement.asType().toString())
        for (injectElement in injectElements) {
            if (injectElement.kind == ElementKind.CONSTRUCTOR) continue
            val resolved = dependencyResolver.resolveDependency(injectElement, target = targetType, skipCheckFromTarget = true, parents = ParensSet())
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

fun Element.isRootScope(): Boolean {
    return isHasAnnotation(ScopeRoot::class.java)
}

fun Element.isChildScope(): Boolean {
    return isHasAnnotation(ScopeChild::class.java)
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