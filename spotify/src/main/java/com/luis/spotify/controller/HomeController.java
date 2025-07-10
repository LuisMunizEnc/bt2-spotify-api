package com.luis.spotify.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeController {
    @GetMapping("/")
    public String home(){
        log.info("Connection public");
        return "Hello";
    }

    @GetMapping("/secured")
    public String secured(){
        log.info("Connection private");
        return "Hello secret";
    }

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        String displayName = "Defalut";
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            displayName = oauth2User.getAttribute("display_name");
            model.addAttribute("displayName", displayName);
            model.addAttribute("attributes", oauth2User.getAttributes());
            System.out.println("Atributos del perfil: " + oauth2User.getAttributes());

            System.out.println("Autenticación completa: " + authentication.getDetails());
            System.out.println(displayName);
        }
        return "profile: "+displayName;
    }
}
