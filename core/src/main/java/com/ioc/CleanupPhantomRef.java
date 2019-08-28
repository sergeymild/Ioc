package com.ioc;

/**
 * Created by sergeygolishnikov on 31/12/2017.
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * A {@link PhantomReference} that has a {@link #cleanup}-function and a {@link #value}. However,
 * there is no reference to the actual object for which this was created.
 *
 * @param <T>
 *          Type of the original object.
 * @param <V>
 *          Type of the value.
 */
final class CleanupPhantomRef<T, V> extends PhantomReference<T> {
    private final V value;

    /** Calls the onCleared action by passing value to the Consumer. */
    void runCleanup() {
        System.out.println("--------------");
        System.out.println("runCleanup " + value);
        System.out.println("--------------");
        if (value instanceof Cleanable) {
            ((Cleanable) value).onCleared();
        }
    }

    @SuppressWarnings("unchecked")
    CleanupPhantomRef(final T referent, final V value) {
        super(referent, (ReferenceQueue<? super T>) ScopeThread.QUEUE);
        this.value = value;
    }
}