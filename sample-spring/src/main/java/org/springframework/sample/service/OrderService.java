package org.springframework.sample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderService {

    @Autowired
    private ProductService productService;

    public void callProduct(){
        System.out.println("this is a order invoke");
        productService.callOrder();
    }

}
