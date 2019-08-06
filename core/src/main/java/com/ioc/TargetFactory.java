package com.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

abstract class TargetFactory {
    private static final String GENERATED_CLASS = "com.ioc.TargetFactoryImplementation";
    protected static Map<Class<?>, Class<?>> map;
    protected static HashMap<Class<?>, Method> cachedInjectMethods;

    private static boolean isLoaded;

    private static void load() {
        if (isLoaded) return;
        try {
            Class.forName(GENERATED_CLASS);
            isLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new IocException("Can't load class TargetFactoryImplementation");
        }
    }

    private static <T> Method provideInjectMethod(final T target) {
        load();
        try {
            Class<?> targetClass = target.getClass();
            Class<?> targetInjectionClass = map.get(targetClass);
            if (targetInjectionClass == null) {
                throw new IocException(String.format("Can't find target injection class for %s type.", targetClass));
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
        } catch (IllegalAccessException e) {
            throw new IocException(e);
        } catch (InvocationTargetException e) {
            throw new IocException(e);
        }
    }
}
