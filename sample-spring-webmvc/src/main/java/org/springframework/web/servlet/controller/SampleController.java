package org.springframework.web.servlet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SampleController {

    @GetMapping
    public String hello() {
        return "index";
    }

}
