package com.ioc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class IocLazy<T> implements Lazy<T>, Cleanable {
    @Nullable
    private T value;

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

    @Override
    public void onCleared() {
        value = null;
    }
}
