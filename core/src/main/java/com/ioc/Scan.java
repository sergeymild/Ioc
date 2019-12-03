package com.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */
@java.lang.annotation.Target({ METHOD })
@Retention(RUNTIME)
@Documented
public @interface Scan {
}
