package com.ioc;

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */

public class Helpers {
    static String importType(Class<?> tClass) {
        return java.lang.String.format("import %s;", tClass.getName());
    }
}
