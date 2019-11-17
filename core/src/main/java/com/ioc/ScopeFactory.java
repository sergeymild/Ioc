package com.ioc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by sergeygolishnikov on 08/11/2017.
 */

@Deprecated
public final class ScopeFactory {
//    private static final ScopeThread thread = new ScopeThread();

    @NonNull
    private static ScopeFactory INSTANCE = new ScopeFactory();
    @NonNull
    // scope root
    //   scopeName
    //
    private final Map<Object, HashMap<String, HashMap<String, Object>>> scopes = new WeakHashMap<>();

    public static <T> void cache(@NonNull Object key, @NonNull String scope, @NonNull String valueName, @NonNull T value) {
//        thread.registerCleanup(key, value);

        HashMap<String, HashMap<String, Object>> keyScopes = INSTANCE.scopes.get(key);
        if (keyScopes == null) {
            keyScopes = new HashMap<>();
            INSTANCE.scopes.put(key, keyScopes);
        }

        HashMap<String, Object> values = keyScopes.get(scope);
        if (values == null) {
            values = new HashMap<>();
            keyScopes.put(scope, values);
        }

        if (!values.containsKey(valueName)) values.put(valueName, value);
    }

    @Nullable
    public static <T> T get(@NonNull String scope, @NonNull String valueName) {
        for (Map.Entry<Object, HashMap<String, HashMap<String, Object>>> keyValues : INSTANCE.scopes.entrySet()) {
            HashMap<String, Object> values = keyValues.getValue().get(scope);
            Object o = values.get(valueName);
            if (o != null) return (T) o;
        }
        return null;
    }

    @Nullable
    public static <T> T get(@NonNull Object key, @NonNull String scope, @NonNull String valueName) {
        HashMap<String, HashMap<String, Object>> keyScopes = INSTANCE.scopes.get(key);
        if (keyScopes == null) return null;
        HashMap<String, Object> values = keyScopes.get(scope);
        if (values == null) return null;
        Object o = values.get(valueName);
        if (o != null) return (T) o;
        return null;
    }

    public static void clear(boolean logException) {
        INSTANCE.scopes.clear();
        try {
            Class<?> aClass = Class.forName("com.ioc.SingletonsClear");
            Method clear = aClass.getDeclaredMethod("clearSingletons");
            clear.invoke(null);
        } catch (Throwable e) {
            if (logException) e.printStackTrace();
        }
    }
}
