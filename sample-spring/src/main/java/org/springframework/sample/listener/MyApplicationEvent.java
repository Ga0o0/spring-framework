package org.springframework.sample.listener;

import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class MyApplicationEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 7099057708183571937L;

    public MyApplicationEvent(String flag) {
        super(flag);
    }

}
