package com.ioc;

import androidx.annotation.Nullable;

public abstract class IocLazy<T> implements Lazy<T> {
    @Nullable
    protected T value;

    @Override
    public boolean isInitialized() {
        return value != null;
    }

    protected abstract T initialize();

    @Nullable
    public T get() {
        if (isInitialized()) return value;
        value = initialize();
        return value;
    }
}
