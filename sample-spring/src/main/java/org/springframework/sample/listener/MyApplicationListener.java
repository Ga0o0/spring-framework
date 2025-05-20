package org.springframework.sample.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationListener implements ApplicationListener<MyApplicationEvent> {
    @Override
    public void onApplicationEvent(MyApplicationEvent event) {
        Object source = event.getSource();
        System.out.println(".............接收事件：" + source);
    }
}
