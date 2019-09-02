package com.example.mylibrary;

import com.ioc.Dependency;

public abstract class Library {
    @Dependency
    public static BottomAdHolderFactory factory() {
        return new BottomAdHolderFactory();
    }
}
