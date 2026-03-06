package com.dfdt.delivery.test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test_1")
    public String getTest01() {
        return "test_01";
    }
}