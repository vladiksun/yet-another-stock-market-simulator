package com.vb.market.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AppEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AppEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(ApplicationEvent event){
        applicationEventPublisher.publishEvent(event);
    }
}
