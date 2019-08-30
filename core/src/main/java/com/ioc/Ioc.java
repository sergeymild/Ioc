package com.ioc;

import java.lang.reflect.Method;

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

public class Ioc {
    public static <T> void inject(T target) {
        TargetFactory.inject(target);
    }

    public static <T> T singleton(Class<T> tClass) {
        return null;
    }

    public static void clear() {
        try {
            Class<?> aClass = Class.forName("com.ioc.SingletonsClear");
            Method clear = aClass.getDeclaredMethod("clearSingletons");
            clear.invoke(null);
        } catch (Throwable e) {

        }
    }
}
