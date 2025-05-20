package org.springframework.sample.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
public class MyService implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;
    @Override
    public void setApplicationEventPublisher(@Nullable ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void update() {
        MyApplicationEvent update = new MyApplicationEvent("update");
        System.out.println(".............发布事件：" + update.getSource());
        applicationEventPublisher.publishEvent(update);
    }

}
