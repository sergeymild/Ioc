package com.ioc.common

import com.ioc.*
import com.ioc.IProcessor.Companion.elementUtils
import com.ioc.IProcessor.Companion.processingEnvironment
import com.ioc.IProcessor.Companion.qualifierFinder
import com.ioc.scanner.AnnotationSetScanner
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

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

fun Element.isGeneric(): Boolean {
    if (!isSupportedType()) return false
    return asType().asDeclared().typeArguments.isNotEmpty()
}

fun Element.getGenericFirstType(): TypeMirror {
    if (!isSupportedType()) throw ProcessorException("Unsupported type $simpleName").setElement(this)
    return asType().getGenericFirstType()
}

fun Element.getGenericFirstOrSelfType(): TypeMirror {
    if (!isSupportedType()) throw ProcessorException("Unsupported type $simpleName").setElement(this)
    if (asType().typeArguments().isEmpty()) return asType()
    return asType().getGenericFirstType()
}

fun <A : Annotation> Element.isHasAnnotation(annotation: Class<A>): Boolean {
    return getAnnotation(annotation) != null
}


fun Element.getPackage(): PackageElement {
    return MoreElements.getPackage(this)
}

fun scanForAnnotation(typeElement: TypeElement, annotation: Class<*>): MutableSet<Element> {
    val annotationType = elementUtils.getTypeElement(annotation.canonicalName)
    val scanner = AnnotationSetScanner(processingEnvironment, mutableSetOf())
    return scanner.scan(typeElement, annotationType)
}

fun RoundEnvironment.collectModuleMethods(
    classesWithDependencyAnnotation: MutableList<Element>,
    methodsWithDependencyAnnotation: MutableList<ExecutableElement>) {
    val dependencies = getElementsAnnotatedWith(Dependency::class.java).toMutableList()

    val queue = LinkedList<Element>(dependencies)
    val checked = mutableSetOf<String>()

    val moduleMethodsSet = mutableSetOf<String>()
    val dependencySet = mutableSetOf<String>()

    while (queue.isNotEmpty()) {
        val dependency = queue.pop()

        val name = qualifierFinder.getModuleMethodQualifier(dependency)
        if (dependency.isNotMethodAndInterface() && dependencySet.add(name)) {
            classesWithDependencyAnnotation.add(dependency)
        } else if (dependency.isMethod()) {
            val method = dependency.asMethod()
            if (moduleMethodsSet.add(name)) {
                methodsWithDependencyAnnotation.add(method)
            } else {
                val tyString = method.asTypeString()
                val addedMethod = methodsWithDependencyAnnotation.firstOrNull { it.asTypeString() == tyString }
                throw ProcessorException("Trying add method `${dependency.enclosingElement.simpleName}.${dependency.simpleName}()` witch already added from: `${addedMethod?.enclosingElement?.simpleName}.${addedMethod?.simpleName}()`").setElement(dependency.enclosingElement)
            }
        }

        if (!dependency.isMethod()) continue

        if (!checked.add(dependency.enclosingElement.asTypeString())) continue

        val superTypes = collectSuperTypes(dependency.enclosingElement.asTypeElement(), false)
        for (superType in superTypes) {
            if (checked.add(superType.toString()))
                queue.addAll(superType.asTypeElement().dependencyMethods())
        }
    }
}

fun RoundEnvironment.rootElementsWithInjectedDependencies(
    targetDependencies: MutableMap<String, MutableSet<Element>>,
    rootTypeElements: MutableList<TypeElement>): List<TypeElement> {

    val checkedSuperclasses = mutableSetOf<String>()

    val injectedElements = getElementsAnnotatedWith(injectJavaType)

    for (dependencyElement in injectedElements) {
        if (dependencyElement.isConstructor()) continue
        val enclosingElement = dependencyElement.enclosingElement
        val key = enclosingElement.asMapKey()

        // if first time meet class element
        if (!targetDependencies.containsKey(key)) {
            val typeElement = enclosingElement.asTypeElement()
            rootTypeElements.add(typeElement)

            // first 3 supertypes for @Inject dependencies
            var superclass = typeElement.superclass
            while (superclass.isAllowForScan()) {
                if (!checkedSuperclasses.add(superclass.toString())) break
                val superclassType = superclass.asTypeElement()
                if (addRootDependencyIfNeed(superclassType, targetDependencies, rootTypeElements)) {
                    superclass = superclassType.superclass
                }
            }
        }

        val dependencies = targetDependencies.getOrPut(key) { mutableSetOf() }
        dependencies.add(dependencyElement)
    }

    // find all module methods with @Scan annotation
    for (method in methodsWithDependencyAnnotation()) {
        if (!method.isHasAnnotation(scanJavaType)) continue
        validateMethodAnnotatedWithTarget(method)
        addRootDependencyIfNeed(method.returnType.asTypeElement(), targetDependencies, rootTypeElements)
    }

    return rootTypeElements
}

fun addRootDependencyIfNeed(
    typeElement: TypeElement,
    targetDependencies: MutableMap<String, MutableSet<Element>>,
    rootTypeElements: MutableList<TypeElement>): Boolean {

    val key = typeElement.asMapKey()
    if (targetDependencies.containsKey(key)) return false
    val found = scanForAnnotation(typeElement, injectJavaType)
    if (found.isEmpty()) return false
    targetDependencies[key] = found
    rootTypeElements.add(typeElement)
    return true
}


fun mapToTargetWithDependencies(
    dependencyResolver: DependencyResolver,
    targetDependencies: MutableMap<String, MutableSet<Element>>,
    rootTypeElements: MutableList<TypeElement>
): Map<TargetType, MutableList<DependencyModel>> {
    try {
        val targetsWithDependencies = mutableMapOf<TargetType, MutableList<DependencyModel>>()

        for (targetTypeElement in rootTypeElements) {
            if (isKotlinCompanionObject(targetTypeElement)) {
                throwCantInjectInCompanionObject(targetTypeElement)
            }
            val targetType = createTarget(targetTypeElement, IProcessor.dependencyFinder)

            val dependencies = targetsWithDependencies.getOrPut(targetType) { mutableListOf() }

            val injectElements = targetDependencies.getValue(targetTypeElement.asMapKey())
            for (injectElement in injectElements) {
                if (injectElement.kind == ElementKind.CONSTRUCTOR) continue
                val resolved = dependencyResolver.resolveDependency(injectElement, target = targetType)
                if (resolved.isLocal) {
                    val getterName = findDependencyGetter(injectElement)
                        .orElse { throwsGetterIsNotFound(injectElement) }
                        .toGetterName()
                    targetType.localScopeDependencies[resolved.originalTypeString] = getterName
                }
                dependencies.add(resolved)
            }
        }

        return targetsWithDependencies
    } catch (e: Throwable) {
        throw e
    }
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
    try {
        toString()
    } catch (e: Throwable) {
        if (e::class.java.canonicalName.contains("com.sun.tools.javac.code.Symbol.CompletionFailure")) {
            return false
            //throw ProcessorException("Class ${method.returnType} is not accessible for: ${ele}, maybe you forgot add module witch contains it.")
        }
    }
    return toString() == "none"
        || toString() == objectJavaType.canonicalName
        || kind == TypeKind.NONE
        || kind == TypeKind.PACKAGE
        || kind == TypeKind.NULL
}

fun TypeMirror?.isAllowForScan(): Boolean {
    this ?: return false
    if (isNotValid()) return false
    val typeString = this.toString()
    return excludedPackages.none { typeString.startsWith(it) }
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

fun DeclaredType.getGenericFirstType(): TypeMirror {
    return typeArguments[0]
}

fun TypeMirror.getGenericFirstType(): TypeMirror {
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
    val declared = MoreTypes.asDeclared(this)
    return if (declared.typeArguments.isNotEmpty()) declared.typeArguments else emptyList()
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

fun Element.isLocalScoped(): Boolean {
    return isHasAnnotation(localScopeJavaType)
}

fun Element.isWeak(): Boolean {
    return asTypeString().startsWith(weakJavaType.canonicalName)
}

fun Element.isLazy(): Boolean {
    return asTypeString().startsWith(lazyJavaType.canonicalName)
}

fun Element.isProvider(): Boolean {
    return asTypeString().startsWith(providerJavaType.canonicalName)
}

fun Element.isPrimitive(): Boolean {
    return asType().kind.isPrimitive
}

fun Element.asMapKey(): String = asTypeString()

fun Element.isViewModel(): Boolean {
    if (isPrimitive()) return false
    return isHasAnnotation(viewModelJavaType)
}

fun Element.isAndroidViewModel(): Boolean {
    if (isPrimitive()) return false
    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.isNotValid()) break
        if (viewModelPackages.contains(superType.toString())) return true
        superType = superType.asTypeElement().superclass
    }
    return false
}

fun Element.isLiveData(): Boolean {
    if (isPrimitive()) return false

    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.isNotValid()) break
        if (liveDataPackages.contains(superType.asElement().toString())) return true
        superType = superType.asTypeElement().superclass
    }
    return false
}

fun Element.isCanHaveViewModel(): Boolean {
    if (isPrimitive()) return false

    var superType = asTypeElement().superclass

    while (superType != null) {
        if (superType.isNotValid()) break
        if (allowedViewModelParents.contains(superType.toString())) return true
        superType = superType.asTypeElement().superclass
    }

    return false
}

fun Element.isCanHaveLiveDataObserver(): Boolean {
    if (isPrimitive()) return false

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
            if (typeArguments.isNotEmpty() && typeArguments.size == 1 && typeArguments[0].toString() == genericType.toString()) {
                return method
            }
        }
    }
    throw exceptionGetterIsNotFound(element)
}

fun Element.toGetterName(): String = if (this is ExecutableElement) "$simpleName()" else simpleName.toString()