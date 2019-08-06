package com.ioc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

public class Ioc {
    private static final String SUFFIX = "Injector";
    private static final int objectHashCode = Object.class.getCanonicalName().hashCode();


    public static <T> T singleton(Class<T> clazz) {
        return SingletonFactory.provide(clazz);
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
