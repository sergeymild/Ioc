package com.ioc;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by sergeygolishnikov on 31/12/2017.
 */
public class ScopeThread extends Thread {
    public static final ReferenceQueue<?> QUEUE = new ReferenceQueue();
    /** The PhantomReferences, so that they do not get removed before their object is removed. */
    private static final Map<CleanupPhantomRef<?, ?>, WeakReference<?>> REFS = new IdentityHashMap<>();
    private boolean running = true;

    public ScopeThread() {
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        start();
    }

    public <V> void registerCleanup(final Object obj, final V value) {
        synchronized (REFS) {
            System.out.println("--registerCleanup " + obj + "  " + value);
            REFS.put(new CleanupPhantomRef<>(obj, value), new WeakReference<>(obj));
        }
    }


    public void run() {
        while (running) {
            final Reference<?> ref;
            try {
                System.out.println("-before get ref");
                ref = QUEUE.remove();
                System.out.println("-after get ref");
            } catch (InterruptedException e) {
                continue;
            }
            if (ref instanceof CleanupPhantomRef)
                cleanupReference((CleanupPhantomRef<?, ?>) ref);
        }
    }

    public static void cleanupReference(final CleanupPhantomRef<?, ?> ref) {
        synchronized (REFS) {
            REFS.remove(ref);
            try {
                ref.runCleanup();
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }
    }
}