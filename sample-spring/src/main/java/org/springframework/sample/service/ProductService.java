package org.springframework.sample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductService {

    @Autowired
    private OrderService orderService;

    public void callOrder(){
        System.out.println("this is a product invoke");
        orderService.callProduct();
    }

}
