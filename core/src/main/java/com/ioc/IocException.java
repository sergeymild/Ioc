package com.ioc;

class IocException extends RuntimeException {
    IocException(String message) {
        super(message);
    }
    IocException(Throwable throwable) {
        super(throwable);
    }
}
