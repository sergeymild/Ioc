package com.ioc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by sergeygolishnikov on 10/07/2017.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface Module {
    Class<?>[] value();
}
