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
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must contains only one parameter.").setElement(method.enclosingElement)
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


fun namedStringForError(named: String?): String {
    return if (named.isNullOrBlank()) "@Default" else "@$named"
}

fun throwMoreThanOneDependencyFoundIfNeed(element: Element, named: String?, models: List<CharSequence>) {
    if (models.size <= 1) return
    throw ProcessorException("Found more than one implementation of `${element.asType()}` with qualifier `${namedStringForError(named)}` [${models.joinToString()}]")
        .setElement(element)
}

fun throwCantFindImplementations(model: Element, target: TargetType?) {
    throw ProcessorException("Can't find methodProvider of `${model.asType()} ${model.enclosingElement}` maybe you forgot add correct @Named, @Qualifier annotations or add @Dependency on provides method, `${target?.element}`").setElement(target?.element)
}

fun throwIfTargetUsedInSingleton(target: TargetType, parentElement: Element, models: List<DependencyModel>) {
    if (models.any { target.isSubtype(it.dependency, it.originalType) }) {
        throw ProcessorException("target can't be user as dependency in Singleton").setElement(parentElement)
    }
}

fun throwSingletonMethodAbstractReturnType(method: ExecutableElement) {
    throw ProcessorException("`${method.enclosingElement}.${method.simpleName}()` annotated with @Singleton must returns implementation not abstract type").setElement(method)
}