package com.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

public class Ioc {
    private static final String SUFFIX = "Injector";
    private static final int objectHashCode = Object.class.getCanonicalName().hashCode();

    private static final HashMap<Class<?>, Lazy<?>> cachedSingletons = new HashMap<>(100);

    private static class IocException extends RuntimeException {
        private IocException(String message) {
            super(message);
        }
        private IocException(Throwable throwable) {
            super(throwable);
        }
    }

    public static <T> T singleton(Class<T> clazz) {
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

    public static <T> void inject(T target) {
        Class<?> clazz = target.getClass();
        while (true) {
            try {
                String packageName = clazz.getPackage().getName();
                String simpleName = clazz.getSimpleName();
                Class<?> injectorClazz = Class.forName(String.format("%s.%s%s", packageName, simpleName, SUFFIX));
                try {
                    Object injector = injectorClazz.newInstance();
                    Method injectMethod = injectorClazz.getDeclaredMethod("inject", clazz);
                    injectMethod.invoke(injector, target);
                    break;
                } catch (NoSuchMethodException e) {
                    throw new IocException(e);
                } catch (InvocationTargetException e) {
                    throw new IocException(e);
                } catch (IllegalAccessException e) {
                    throw new IocException(e);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException ignored) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.getCanonicalName().hashCode() == objectHashCode)
                    throw new IocException("Can't find Injector class for " + target.getClass().getSimpleName());
            }
        }
    }
}
