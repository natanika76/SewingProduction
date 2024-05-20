package ru.vilas.sewing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {


    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("requestURI", "/login");
        return "login";

    }

    // TODO: Реализовать этот метод
    @GetMapping("")
    public String redirectToTarget() {
        return "redirect:/categories";
    }

    @GetMapping("/index")
    public String redirectFromIndex() {
        return "redirect:/categories";
    }
}
