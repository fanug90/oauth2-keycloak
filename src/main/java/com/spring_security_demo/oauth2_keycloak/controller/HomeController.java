package com.spring_security_demo.oauth2_keycloak.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OidcUser oidcUser, Model model){
        model.addAttribute("username", oidcUser.getPreferredUsername());
        model.addAttribute("email", oidcUser.getEmail());
        model.addAttribute("name", oidcUser.getFullName());
        model.addAttribute("roles", oidcUser.getAuthorities());

        return "home";
    }

    @GetMapping("/user")
    public String user() {
        return "user";
    }

    @GetMapping("/manager")
    public String manager() {
        return "manager";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

}
