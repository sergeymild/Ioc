package com.example.mylibrary;

import com.ioc.Dependency;

public abstract class Library extends Library2 {
    @Dependency
    public static BottomAdHolderFactory factory() {
        return new BottomAdHolderFactory();
    }
}
