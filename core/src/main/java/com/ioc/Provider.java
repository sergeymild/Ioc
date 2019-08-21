package com.ioc;

public interface Provider<T> {

    /**
     * Provides a fully-constructed and injected instance of {@code T}.
     *
     * @throws RuntimeException if the injector encounters an error while
     *  providing an instance. For example, if an injectable member on
     *  {@code T} throws an exception, the injector may wrap the exception
     *  and throw it to the caller of {@code get()}. Callers should not try
     *  to handle such exceptions as the behavior may vary across injector
     *  implementations and even different configurations of the same injector.
     */
    T get();
}
