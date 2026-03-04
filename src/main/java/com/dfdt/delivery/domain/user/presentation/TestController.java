package com.dfdt.delivery.domain.user.presentation;

import com.dfdt.delivery.domain.auth.infrastructure.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/me")
    public String getMe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return "No user";
        }
        return "Hello " + customUserDetails.getName() + " (" + customUserDetails.getUsername() + ")";
    }
}