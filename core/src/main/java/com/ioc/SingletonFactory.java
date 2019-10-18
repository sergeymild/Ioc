package com.ioc;

import java.util.HashMap;
import java.util.Map;

public abstract class SingletonFactory {
    private static final String GENERATED_CLASS = "com.ioc.SingletonsFactoryImplementation";
    static Map<Class<?>, Class<?>> map;
    static HashMap<Class<?>, Object> cachedSingletons;

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

    public static <T> T provide(final Class<T> tClass) {
        load();
        try {
            Class<?> singleton = map.get(tClass);
            if (singleton == null) {
                throw new IocException(String.format("Can't find singleton for %s type.", tClass));
            }

            Object instance = cachedSingletons.get(singleton);
            if (instance == null) {
                instance = ((Provider)singleton.newInstance()).get();
                cachedSingletons.put(singleton, instance);
            }
            return (T) instance;
        } catch (IllegalAccessException e) {
            throw new IocException(e);
        } catch (InstantiationException e) {
            throw new IocException(e);
        }
    }

    public static void clear() {
        for (Map.Entry<Class<?>, Object> entry : cachedSingletons.entrySet()) {
            if (entry.getValue() instanceof Cleanable) {
                ((Cleanable) entry.getValue()).onCleared();
            }
        }
        cachedSingletons.clear();
    }
}
