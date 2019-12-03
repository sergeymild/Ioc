package com.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

abstract class TargetFactory {
    private static final int objectHashCode = Object.class.getCanonicalName().hashCode();
    protected static Map<Class<?>, Class<?>> map = new HashMap<>(100);
    protected static Map<Class<?>, Method> cachedInjectMethods = new HashMap<>(100);

    private static <T> Method provideInjectMethod(final T target) {

        try {
            Class<?> targetClass = target.getClass();
            Class<?> targetInjectionClass = map.get(targetClass);
            if (targetInjectionClass == null) {

                while (true) {
                    try {
                        targetInjectionClass = Class.forName(targetClass.getCanonicalName().concat("Injector"));
                        map.put(targetClass, targetInjectionClass);
                        break;
                    } catch (ClassNotFoundException ignored) {
                        targetClass = targetClass.getSuperclass();
                        if (targetClass == null || targetClass.getCanonicalName().hashCode() == objectHashCode)
                            throw new IocException("Can't find Injector class for " + target.getClass().getSimpleName());
                    }
                }
            }

            Method targetInjectMethod = cachedInjectMethods.get(targetInjectionClass);
            if (targetInjectMethod == null) {
                targetInjectMethod = targetInjectionClass.getDeclaredMethod("inject", targetClass);
                cachedInjectMethods.put(targetInjectionClass, targetInjectMethod);
            }
            return targetInjectMethod;
        } catch (NoSuchMethodException e) {
            throw new IocException(e);
        }
    }

    static <T> void inject(final T target) {
        try {
            provideInjectMethod(target).invoke(null, target);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IocException(e);
        }
    }
}
