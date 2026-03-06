package com.dfdt.delivery.deploy_test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeployTestController {
    @GetMapping("/test_1")
    public String getTest01() {
        return "test_01";
    }
}