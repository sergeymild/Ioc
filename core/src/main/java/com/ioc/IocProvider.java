package com.ioc;

import androidx.annotation.Nullable;

import javax.inject.Provider;

public abstract class IocProvider<T> implements Provider<T> {
    protected abstract T initialize();

    @Nullable
    public T get() {
        return initialize();
    }
}
