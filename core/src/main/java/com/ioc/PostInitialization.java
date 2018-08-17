package com.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by sergeygolishnikov on 02/01/2018.
 */

@Target({ METHOD })
@Retention(RUNTIME)
@Documented
public @interface PostInitialization {
}
