package com.api.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/public")
    public String publicApi() {
        return "This is a public Api";
    }

    @GetMapping("/private")
    public String privateApi(OAuth2AuthenticationToken token) {
        return "Authenticated"+token.getPrincipal().getAttribute("email");
    }
}
