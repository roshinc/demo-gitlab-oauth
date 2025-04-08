package dev.roshin.login.demogitlaboauth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("name", principal.getAttribute("name"));
        model.addAttribute("username", principal.getAttribute("username"));
        model.addAttribute("avatar", principal.getAttribute("avatar_url"));
        return "home";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "home";
    }

}
