package com.ioc.common

import com.ioc.*
import com.ioc.IProcessor.Companion.elementUtils
import com.ioc.IProcessor.Companion.processingEnvironment
import com.ioc.Scan
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import java.lang.ref.WeakReference
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementScanner8

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

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

fun Element.asMethod(): ExecutableElement {
    return this as ExecutableElement
}

fun Element.isPrivate(): Boolean {
    return modifiers.contains(Modifier.PRIVATE)
}

fun Element.isPublic(): Boolean {
    return modifiers.contains(Modifier.PUBLIC)
}

fun Element.isConstructor(): Boolean {
    return kind == ElementKind.CONSTRUCTOR
}

fun Element.getGenericFirstType(): Element {
    if (!isSupportedType()) throw ProcessorException("Unsupported type $simpleName").setElement(this)
    return asType().getGenericFirstType()
}

fun Element.getGenericFirstOrSelfType(): Element {
    if (!isSupportedType()) throw ProcessorException("Unsupported type $simpleName").setElement(this)
    if (asType().typeArguments().isEmpty()) return this
    return asType().getGenericFirstType()
}

fun <A : Annotation> Element.isHasAnnotation(annotation: Class<A>): Boolean {
    return getAnnotation(annotation) != null
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

fun scanForAnnotation(typeElement: TypeElement, annotation: Class<*>): MutableSet<Element> {
    val annotationType = elementUtils.getTypeElement(annotation.canonicalName)
    val injectElements = mutableSetOf<Element>()
    val scanner = AnnotationSetScanner(processingEnvironment, injectElements)
    return scanner.scan(typeElement, annotationType)
}

fun RoundEnvironment.rootElementsWithInjectedDependencies(): List<TypeElement> {
    targetDependencies.clear()
    rootTypeElements.clear()

    val injectedElements = getElementsAnnotatedWith(injectJavaType)

    for (dependencyElement in injectedElements) {
        if (dependencyElement.isConstructor()) continue
        val enclosingElement = dependencyElement.enclosingElement
        val key = enclosingElement.asType().toString()

        // if first time meet class element
        if (!targetDependencies.containsKey(key)) {
            val typeElement = enclosingElement.asTypeElement()
            rootTypeElements.add(typeElement)
        }

        val dependencies = targetDependencies.getOrPut(key) { mutableSetOf() }
        dependencies.add(dependencyElement)
    }

    // find all module methods with @Scan annotation
    for (method in methodsWithDependencyAnnotation()) {
        if (!method.isHasAnnotation(scanJavaType)) continue
        validateMethodAnnotatedWithTarget(method)
        val targetTypeElement = method.returnType.asTypeElement()
        val key = targetTypeElement.asType().toString()
        if (!targetDependencies.containsKey(key)) {
            val found = scanForAnnotation(targetTypeElement, injectJavaType)
            targetDependencies[key] = found.toMutableSet()
            rootTypeElements.add(targetTypeElement)
        }
    }

    return rootTypeElements
}

fun RoundEnvironment.findDependenciesInParents() {
    val uniqueRootTypeElements = mutableSetOf<String>()

    for (childElement in getElementsAnnotatedWith(injectParentDependenciesJavaType)) {
        val typeElement = childElement.asTypeElement()
        var superclass = typeElement.superclass
        var isDependenciesFound = false
        while (!superclass.isNotValid()) {
            if (uniqueRootTypeElements.contains(superclass.toString())) continue
            val superclassTypeElement = superclass.asTypeElement()

            val found = scanForAnnotation(superclassTypeElement, injectJavaType)

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
        val targetType = createTarget(targetTypeElement, IProcessor.dependencyFinder)

        val dependencies = targetsWithDependencies.getOrPut(targetType) { mutableListOf() }

        val injectElements = targetDependencies.getValue(targetTypeElement.asType().toString())
        for (injectElement in injectElements) {
            if (injectElement.kind == ElementKind.CONSTRUCTOR) continue
            val resolved = dependencyResolver.resolveDependency(injectElement, target = targetType)
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

fun Element.isNotMethodAndInterface(): Boolean {
    return kind != ElementKind.METHOD && kind != ElementKind.INTERFACE
}

fun TypeMirror.isNotValid(): Boolean {
    return toString() == objectJavaType.canonicalName
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

fun Element.asClassName(): TypeName {
    return asType().asClassName()
}

fun TypeMirror.asClassName(): TypeName {
    return ClassName.get(this)
}

fun TypeMirror.asTypeElement(): TypeElement {
    return MoreTypes.asTypeElement(this)
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

fun Name.decapitalize(): String {
    return toString().decapitalize()
}

fun CharSequence.titleize(): String {
    return toString().capitalize()
}

fun Element.isSingleton(): Boolean {
    return isHasAnnotation(singletonJavaType)
}

fun Element.isWeak(): Boolean {
    return asType().toString().startsWith(weakJavaType.canonicalName)
}

fun Element.isLazy(): Boolean {
    return asType().toString().startsWith(lazyJavaType.canonicalName)
}

fun Element.isProvider(): Boolean {
    return asType().toString().startsWith(providerJavaType.canonicalName)
}

fun Element.isPrimitive(): Boolean {
    return asType().kind.isPrimitive
}

fun Element.isViewModel(): Boolean {
    if (asType().kind.isPrimitive) return false
    return isHasAnnotation(viewModelJavaType)
}

fun Element.isAndroidViewModel(): Boolean {
    if (asType().kind.isPrimitive) return false
    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.kind == TypeKind.NONE) break
        if (viewModelPackages.contains(superType.toString())) return true
        superType = superType.asTypeElement().superclass
    }
    return false
}

fun Element.isLiveData(): Boolean {
    if (asType().kind.isPrimitive) return false

    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.kind == TypeKind.NONE) break
        if (liveDataPackages.contains(superType.asElement().toString())) return true
        superType = superType.asTypeElement().superclass
    }
    return false
}

fun Element.isCanHaveViewModel(): Boolean {
    if (asType().kind.isPrimitive) return false

    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.kind == TypeKind.NONE) break
        if (allowedViewModelParents.contains(superType.toString())) return true
        superType = superType.asTypeElement().superclass
    }

    return false
}

fun Element.isCanHaveLiveDataObserver(): Boolean {
    if (asType().kind.isPrimitive) return false

    val typeElement = asTypeElement()

    val supertypes = mutableSetOf<TypeMirror>()
    val queue = LinkedList<TypeMirror>()
    queue.addAll(typeElement.interfaces)
    queue.add(typeElement.superclass)

    while (queue.isNotEmpty()) {
        val supertype = queue.pop()
        if (supertype.isNotValid()) continue
        if (supertype.toString() == lifecycleOwner) return true
        supertypes.add(supertype)
        val superclass = supertype.asTypeElement()
        queue.addAll(superclass.interfaces)
        queue.add(superclass.superclass)
    }

    return false
}

fun Element.isMethod(): Boolean {
    return kind == ElementKind.METHOD
}

fun Element.isClass(): Boolean {
    return kind == ElementKind.CLASS
}

fun Element.isInterface(): Boolean {
    return kind == ElementKind.INTERFACE
}

fun Element.isAbstract(): Boolean {
    return modifiers.contains(Modifier.ABSTRACT)
}

fun Element.isStatic(): Boolean {
    return modifiers.contains(Modifier.STATIC)
}

fun ExecutableElement.isContainsOneParameterOf(element: Element): Boolean {
    return parameters.size == 1 && parameters[0].isEqualTo(element)
}

@Throws(ProcessorException::class)
fun findDependencySetter(element: Element): ExecutableElement? {
    return element.enclosingElement.methods {
        it.isPublic() && it.isContainsOneParameterOf(element)
    }.firstOrNull()
}

@Throws(ProcessorException::class)
fun findDependencyGetter(element: Element): Element? {
    if (element.isPublic()) return element
    return element.enclosingElement.methods {
        it.isPublic() && it.parameters.isEmpty() && it.returnType.isEqualTo(element)
    }.firstOrNull()
}

fun collectSuperTypes(typeElement: TypeElement?, includeSelf: Boolean = false): Set<TypeMirror> {
    typeElement ?: return emptySet()

    val supertypes = mutableSetOf<TypeMirror>()
    val queue = LinkedList<TypeMirror>()
    if (includeSelf) queue.add(typeElement.asType())
    queue.addAll(typeElement.interfaces)
    queue.add(typeElement.superclass)

    while (queue.isNotEmpty()) {
        val supertype = queue.pop()
        if (supertype.isNotValid()) continue
        supertypes.add(supertype)
        val superclass = supertype.asTypeElement()
        queue.addAll(superclass.interfaces)
        queue.add(superclass.superclass)
    }

    return supertypes
}

@Throws(ProcessorException::class)
fun findDependencyGetterFromTypeOrSuperType(element: Element): Element {
    if (element.isPublic()) return element
    val supertypes = collectSuperTypes(element.asTypeElement(), includeSelf = true)
    val genericType = element.getGenericFirstOrSelfType()
    for (method in element.enclosingElement.methods()) {
        val returnType = method.returnType.asElement().toString()
        if (supertypes.any { IProcessor.types.erasure(it).toString() == returnType }) {
            val typeArguments = method.returnType.typeArguments()
            if (typeArguments.isNotEmpty() && typeArguments.size == 1 && typeArguments[0].isEqualTo(genericType)) {
                return method
            }
        }
    }
    throw exceptionGetterIsNotFound(element)
}

fun Element.toGetterName(): String = if (this is ExecutableElement) "$simpleName()" else simpleName.toString()