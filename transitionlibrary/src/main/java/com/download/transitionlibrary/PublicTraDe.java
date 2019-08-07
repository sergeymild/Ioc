package com.download.transitionlibrary;

import com.ioc.Ioc;

import javax.inject.Inject;

public class PublicTraDe {
    @Inject
    public TraDe traDe;

    public PublicTraDe() {
        Ioc.inject(this);
    }
}
