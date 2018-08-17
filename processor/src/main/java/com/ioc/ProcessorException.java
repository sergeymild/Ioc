package com.ioc;


import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;

/**
 * Created by sergeygolishnikov on 08/08/2017.
 */

public class ProcessorException extends RuntimeException {
    @Nullable
    public Element element;
    public ProcessorException(String message, Object... args) {
        super(String.format(message, args));
    }

    public ProcessorException setElement(@Nullable Element element) {
        this.element = element;
        return this;
    }

    @Override
    public String toString() {
        return "ProcessorException{" +
                "element=" + (element != null ? element : "null") +
                '}';
    }
}