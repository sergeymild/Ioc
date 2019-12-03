package com.ioc;

/**
 * Created by sergeygolishnikov on 22/12/2017.
 */

public interface Lazy<T> extends Provider<T> {
    boolean isInitialized();
}
