package com.ioc

import com.ioc.common.asTypeElement
import com.ioc.common.isCanHaveViewModel
import com.ioc.common.isInterface
import com.ioc.common.message
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

fun throwMoreThanOneDependencyFoundIfNeed(element: Element, named: String?, models: List<DependencyModel>) {
    if (models.size <= 1) return
    throw ProcessorException("Found more than one implementation of `${element.asType()}` with qualifier `${namedStringForError(named)}` [${models.joinToString { it.originalTypeString }}]")
        .setElement(element)
}

fun throwCantFindImplementations(model: DependencyModel, target: TargetType) {
    throw ProcessorException("Can't find implementations of `${model.dependency.asType()} ${model.dependency}` maybe you forgot add correct @Named, @Qualifier or @Scope annotations or add @Dependency on provides method, `${target.element}`").setElement(target.element)
}