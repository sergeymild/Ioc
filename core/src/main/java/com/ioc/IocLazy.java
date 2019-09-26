package com.ioc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class IocLazy<T> implements Lazy<T> {
    @Nullable
    protected T value;

    @Override
    public boolean isInitialized() {
        return value != null;
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract T initialize();

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public T get() {
        if (isInitialized()) return value;
        value = initialize();
        return value;
    }
}
