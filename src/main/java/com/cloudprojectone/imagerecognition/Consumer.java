package com.cloudprojectone.imagerecognition;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @JmsListener(destination = "${amazonProperties.requestQueue}")
    public void processMessage(String msg) {
        System.out.println(msg);
    }
}
