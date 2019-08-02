package com.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

class SingletonManager {
    private static final HashMap<Class<?>, Lazy<?>> cachedSingletons = new HashMap<>(100);

    static <T> T singleton(Class<T> clazz) {
        try {
            Lazy<?> instance = cachedSingletons.get(clazz);
            if (instance == null) {
                Class<?> singletonClass = Class.forName(clazz.getCanonicalName() + "Singleton");
                Method methodInstance = singletonClass.getDeclaredMethod("getInstance");
                instance = (Lazy<?>) methodInstance.invoke(singletonClass);
                cachedSingletons.put(clazz, instance);
            }
            return (T) instance.get();
        } catch (ClassNotFoundException e) {
            throw new IocException(e);
        } catch (NoSuchMethodException e) {
            throw new IocException(e);
        } catch (IllegalAccessException e) {
            throw new IocException(e);
        } catch (InvocationTargetException e) {
            throw new IocException(e);
        }
    }
}
