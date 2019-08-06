package com.ioc;

/**
 * Created by sergeygolishnikov on 11/07/2017.
 */

public class Ioc {
    public static <T> T singleton(Class<T> clazz) {
        return SingletonFactory.provide(clazz);
    }

    public static <T> void inject(T target) {
        TargetFactory.inject(target);
    }
}
