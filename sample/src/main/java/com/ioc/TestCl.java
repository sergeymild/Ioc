package com.ioc;

@Singleton
public class TestCl {
    static {
        System.out.println("--- call static method");
    }
}
