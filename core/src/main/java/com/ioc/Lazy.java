package com.ioc;



import javax.inject.Provider;

/**
 * Created by sergeygolishnikov on 22/12/2017.
 */

public interface Lazy<T> extends Provider<T> {
    boolean isInitialized();
}
