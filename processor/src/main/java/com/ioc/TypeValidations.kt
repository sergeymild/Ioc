package com.ioc

import com.ioc.common.*
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind


@Throws(ProcessorException::class)
fun validateObserverMethod(method: ExecutableElement) {
    if (method.modifiers.contains(Modifier.PRIVATE)) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must be public.").setElement(method.enclosingElement)
    }

    if (method.parameters.size > 1) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must contains only one parameter.").setElement(method.enclosingElement)
    }

    if (method.returnType.kind != TypeKind.VOID) {
        throw ProcessorException("method ${method.enclosingElement}.$method annotated with @DataObserver must not contains return type.").setElement(method.enclosingElement)
    }
}

@Throws(ProcessorException::class)
fun validateTargetForLiveDataObserver(typeElement: TypeElement) {
    if (!typeElement.isCanHaveViewModel()) {
        throw ProcessorException("@DataObserver methods may be placed only in Activity or Fragment but was found in $typeElement.").setElement(typeElement)
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

fun validateSingletonUsage(targetsWithDependencies: Map<TargetType, MutableList<DependencyModel>>) {
    // check how ofter used singletons
    val counter = mutableMapOf<String, Int>()
    for (target in targetsWithDependencies) {
        val queue = LinkedList(target.value)
        while (queue.isNotEmpty()) {
            val dep = queue.pop()
            if (dep.isSingleton) {
                val count = counter.getOrPut(dep.dependencyTypeString) { 0 }
                counter[dep.dependencyTypeString] = count + 1
            }
            queue.addAll(dep.dependencies)
        }
    }

    for (entry in counter) {
        if (entry.value == 1) message("@Singleton is redundant for dependency: ${entry.key}")
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

fun throwCantFindImplementations(model: Element, target: TargetType) {
    throw ProcessorException("Can't find methodProvider of `${model.asType()} ${model.enclosingElement}` maybe you forgot add correct @Named, @Qualifier annotations or add @Dependency on provides method, `${target.element}`").setElement(target.element)
}

fun throwIfTargetUsedInSingleton(target: TargetType, parentElement: Element, models: List<DependencyModel>) {
    if (models.any { target.isSubtype(it.dependency, it.originalType) }) {
        throw ProcessorException("target can't be user as dependency in Singleton").setElement(parentElement)
    }
}

fun throwSingletonMethodAbstractReturnType(method: ExecutableElement) {
    throw ProcessorException("`${method.enclosingElement}.${method.simpleName}()` annotated with @Singleton must returns implementation not abstract type").setElement(method)
}