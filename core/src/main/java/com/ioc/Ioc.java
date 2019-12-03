package com.ioc;

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

public class Ioc {
    public static <T> void inject(T target) {
        TargetFactory.inject(target);
    }

    public static <T> T getSingleton(Class<T> clazz) {
        return SingletonFactory.provide(clazz);
    }

    public static void clearSingletons(boolean isDebug) {
        SingletonFactory.clear(isDebug);
    }
}
