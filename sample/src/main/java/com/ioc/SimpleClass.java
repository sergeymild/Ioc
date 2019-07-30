package com.ioc;

import androidx.lifecycle.Observer;

public class SimpleClass {

    public SimpleClass() {
        new MyViewModel().getLiveData().observe(null, new Observer<String>() {
            @Override
            public void onChanged(String s) {

            }
        });
    }
}
