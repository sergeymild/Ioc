package com.download.transitionlibrary;

import com.ioc.Inject;
import com.ioc.Ioc;



public class PublicTraDe {
    @Inject
    public TraDe traDe;

    public PublicTraDe() {
        Ioc.inject(this);
    }
}
