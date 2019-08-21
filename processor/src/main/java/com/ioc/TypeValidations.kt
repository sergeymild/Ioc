package com.ioc

import com.ioc.common.*
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind


@Throws(ProcessorException::class)
fun validateObserverMethod(method: ExecutableElement) {
    if (method.modifiers.contains(Modifier.PRIVATE)) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must be public.").setElement(method.enclosingElement)
    }

    if (method.parameters.size > 1 || method.parameters.isEmpty()) {
        throwsDataObserverMustHaveOnlyOneParameter(method)
    }

    if (method.returnType.kind != TypeKind.VOID) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must not contains return type.").setElement(method.enclosingElement)
    }
}

@Throws(ProcessorException::class)
fun validateTargetForLiveDataObserver(typeElement: TypeElement) {
    if (!typeElement.isCanHaveLiveDataObserver()) {
        throw ProcessorException("@DataObserver methods may be placed only in Activity or Fragment but was found in $typeElement.").setElement(typeElement)
    }
}

@Throws(ProcessorException::class)
fun validateViewModelForLiveData(element: VariableElement) {
    if (element.isAndroidViewModel() && !element.enclosingElement.isCanHaveViewModel()) {
        throw ProcessorException("@DataObserver methods may be placed only in Activity or Fragment but was found in ${element.enclosingElement}.").setElement(element)
    }
}

fun validateAbstractModuleMethodProvider(method: ExecutableElement) {
    val implementationType = method.parameters.firstOrNull()?.asTypeElement()
    val type = method.returnType.asTypeElement()

    if (method.parameters.size > 1) {
        throw ProcessorException("${method.enclosingElement.simpleName}.${method.simpleName} must have exactly one parameter.")
    }

    //public abstract InterfaceType method();
    if (implementationType == null && type.isInterface()) {
        throw ProcessorException("${method.enclosingElement.simpleName}.${method.simpleName}() returns $type which is interface also must contain implementation as parameter").setElement(method)
    }

    //public abstract InterfaceType method(InterfaceType);
    if (implementationType?.isInterface() == true) {
        throw ProcessorException("${method.enclosingElement.simpleName}.${method.simpleName}($implementationType) returns $type which is interface also contains interface as parameter must be implementation").setElement(method)
    }
}

fun validateMethodAnnotatedWithTarget(method: ExecutableElement) {
    if (method.parameters.isNotEmpty()) {
        throw ProcessorException("${method.enclosingElement.simpleName}.${method.simpleName} with @Scan must have no parameters.")
    }

    if (!method.isAbstract()) {
        throw ProcessorException("${method.enclosingElement.simpleName}.${method.simpleName} with @Scan must be abstract.")
    }

    if (method.returnType.kind == TypeKind.VOID) {
        throw ProcessorException("${method.enclosingElement.simpleName}.${method.simpleName} with @Scan must return type for scan")
    }
}

fun validateModuleMethod(isKotlinModule: Boolean, provider: ExecutableElement) {
    if (!provider.isStatic() && !isKotlinModule) {
        throw ProcessorException("${provider.enclosingElement.simpleName}.${provider.simpleName}() is annotated with @Dependency must be static and public").setElement(provider)
    }
}

fun validateIsAllowCanHaveViewModel(viewModel: Element, targetElement: TypeElement) {
    if (targetElement.isCanHaveViewModel()) return
    throw ProcessorException("@Inject annotation is placed on `${viewModel.asType()}` class which declared not in either FragmentActivity or Fragment").setElement(viewModel)
}

fun validateSingletonClass(element: Element) {
    if (element.isClass() && !element.isPublic()) {
        throw ProcessorException("${element.asTypeString()} annotated with @Singleton must be public").setElement(element)
    }
}

fun validateSingletonMethod(element: Element) {
    if (!element.isMethod()) return
    if (element.asMethod().returnType.kind == TypeKind.VOID) {
        throw ProcessorException("${element.enclosingElement.asTypeString()}.${element.simpleName}() annotated with @Singleton must return type").setElement(element)
    }

    if (element.asMethod().returnType.isNotValid()) {
        throw ProcessorException("${element.enclosingElement.asTypeString()}.${element.simpleName}() annotated with @Singleton return type is not valid.").setElement(element)
    }
}

fun validatePostInitializationMethod(method: ExecutableElement) {
    if (method.isPrivate()) {
        throw ProcessorException("@PostInitialization placed on `${method.simpleName}` in ${method.enclosingElement} with private access").setElement(method)
    }

    if (method.parameters.isNotEmpty()) {
        throw ProcessorException("@PostInitialization placed on `${method.simpleName}` in ${method.enclosingElement} must not have parameters").setElement(method)
    }

    if (method.returnType.kind != TypeKind.VOID) {
        throw ProcessorException("@PostInitialization placed on `${method.simpleName}` in ${method.enclosingElement} must not have return type").setElement(method)
    }
}

fun validateContainsSetterAndGetterInParent(element: Element) {
    findDependencySetter(element).orElse { throwsSetterIsNotFound(element) }
    findDependencyGetter(element).orElse { throwsGetterIsNotFound(element) }
}

fun validateSetterMethod(method: ExecutableElement) {
    if (method.parameters.size > 1) {
        throw ProcessorException("@Inject annotation is placed on method `$method` in `${method.enclosingElement.simpleName}` with more than one parameter").setElement(method)
    }

    if (method.isPrivate()) {
        throw ProcessorException("@Inject annotation is placed on method `$method` in `${method.enclosingElement.simpleName}` with private access").setElement(method)
    }
}

fun validateConstructorParameter(element: Element) {
    if (element.isPrimitive()) {
        throw ProcessorException("Constructor used primitive type").setElement(element)
    }
}

/**
 *  Exceptions
 * */

fun exceptionGetterIsNotFound(element: Element): ProcessorException {
    return ProcessorException("@Inject annotation placed on field `${element.simpleName}` in `${element.enclosingElement.simpleName}` with private access and which does't have public getter method.").setElement(element)
}

fun exceptionDidNotFindConstructorOrMethodProvider(element: Element): ProcessorException {
    return ProcessorException("Can't find default constructor or provide method for `${element.asType()}`").setElement(element)
}

/**
 *  Throws
 * */

@Throws(Throwable::class)
fun throwsDataObserverMustHaveOnlyOneParameter(method: Element) {
    throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must contains only one parameter.").setElement(method.enclosingElement)
}

@Throws(Throwable::class)
fun throwsGetterIsNotFound(element: Element) {
    throw exceptionGetterIsNotFound(element)
}

fun throwsMoreThanOneDependencyFoundIfNeed(element: Element, named: String?, models: List<CharSequence>) {
    if (models.size <= 1) return
    throw ProcessorException("Found more than one implementation of `${element.asType()}` with qualifier `${namedStringForError(named)}` [${models.joinToString()}]")
        .setElement(element)
}

fun throwsCantFindImplementations(model: Element, target: TargetType?) {
    throw ProcessorException("Can't find method provider of `${model.asType()} ${model.enclosingElement}` maybe you forgot add correct @Named, @Qualifier annotations or add @Dependency on provides method, `${target?.element}`").setElement(model)
}

fun throwsIfTargetUsedInSingleton(target: TargetType, parentElement: Element, models: List<DependencyModel>) {
    if (models.any { target.isSubtype(it.dependency, it.originalType) }) {
        throw ProcessorException("target can't be user as dependency in Singleton").setElement(parentElement)
    }
}

fun throwsSingletonMethodAbstractReturnType(method: ExecutableElement) {
    throw ProcessorException("`${method.enclosingElement}.${method.simpleName}()` annotated with @Singleton must returns implementation not abstract type").setElement(method)
}

@Throws(ProcessorException::class)
fun throwsSetterIsNotFound(element: Element) {
    throw ProcessorException("@Inject annotation placed on field `${element.simpleName}` in `${element.enclosingElement.simpleName}` with private access and which does't have public setter method.").setElement(element)
}

fun throwsInjectPlacedOnPrivateMethod(element: Element) {
    throw ProcessorException("@Inject annotation is placed on method `$element` in `${element.enclosingElement}` with private access").setElement(element)
}

fun throwsConstructorIsPrivate(element: Element) {
    throw ProcessorException("${element.enclosingElement}.${element.simpleName} contains @Inject must be public").setElement(element)
}

fun throwsConstructorHasUnsupportedParameters(element: Element) {
    throw ProcessorException("@Inject annotation placed on constructor in ${element.enclosingElement} which have unsupported parameters.").setElement(element)
}

fun throwsDidNotFindSuitableConstructor(element: Element) {
    throw ProcessorException("Cant find suitable constructors ${element.enclosingElement}").setElement(element)
}

@Throws(ProcessorException::class)
fun throwTargetMustBePublic(element: Element) {
    throw ProcessorException("${element.asTypeString()} must be public.").setElement(element)
}

@Throws(ProcessorException::class)
fun throwAmbiguousImplementationsFound(element: Element, found: Element) {
    throw ProcessorException("Ambiguous dependency $element is class by itself but also found $found try to add @Qualifier annotation.").setElement(element)
}