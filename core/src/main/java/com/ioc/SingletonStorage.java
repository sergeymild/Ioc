package com.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

abstract class SingletonStorage {
    private static final String GENERATED_CLASS = "com.ioc.SingletonsFactoryImplementation";
    static Map<Class<?>, Class<?>> map;
    static HashMap<Class<?>, Lazy<?>> cachedSingletons;

    private static boolean isLoaded;

    private static void load() {
        if (isLoaded) return;
        try {
            Class.forName(GENERATED_CLASS);
            isLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new IocException("Can't load class SingletonsFactoryImplementation");
        }
    }

    static <T> T provide(final Class<T> tClass) {
        load();
        try {
            Class<?> singleton = map.get(tClass);
            if (singleton == null) {
                throw new IocException(String.format("Can't find singleton for %s type.", tClass));
            }

            Lazy<?> instance = cachedSingletons.get(singleton);
            if (instance == null) {
                Method methodInstance = singleton.getDeclaredMethod("getInstance");
                instance = (Lazy<?>) methodInstance.invoke(singleton);
                cachedSingletons.put(singleton, instance);
            }
            return (T) instance.get();
        } catch (NoSuchMethodException e) {
            throw new IocException(e);
        } catch (IllegalAccessException e) {
            throw new IocException(e);
        } catch (InvocationTargetException e) {
            throw new IocException(e);
        }
    }
}
