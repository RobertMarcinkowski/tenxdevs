package eu.robm15.tenxdevs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    @GetMapping("/landing")
    public String landingPage() {
        return "landing";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/app")
    public String app() {
        return "app";
    }
}
