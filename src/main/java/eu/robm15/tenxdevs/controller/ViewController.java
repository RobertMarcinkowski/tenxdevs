package eu.robm15.tenxdevs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class ViewController {

    @Autowired
    private Environment environment;

    private void addEnvironmentAttributes(Model model) {
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        // Determine environment type and ribbon display
        String environmentType = null;
        String ribbonColor = null;
        boolean useMockAuth = activeProfile.equals("localh2");

        if (activeProfile.equals("localh2") || activeProfile.equals("localsupabase")) {
            environmentType = "LOCAL ENVIRONMENT";
            ribbonColor = "#ffc107"; // Yellow/amber for local
        } else if (activeProfile.equals("develop")) {
            environmentType = "TEST ENVIRONMENT";
            ribbonColor = "#17a2b8"; // Blue for test/develop
        }
        // No ribbon for prod profile

        model.addAttribute("environmentType", environmentType);
        model.addAttribute("ribbonColor", ribbonColor);
        model.addAttribute("activeProfile", activeProfile);
        model.addAttribute("useMockAuth", useMockAuth);
    }

    @GetMapping("/")
    public String landing(Model model) {
        addEnvironmentAttributes(model);
        return "landing";
    }

    @GetMapping("/landing")
    public String landingPage(Model model) {
        addEnvironmentAttributes(model);
        return "landing";
    }

    @GetMapping("/login")
    public String login(Model model) {
        addEnvironmentAttributes(model);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        addEnvironmentAttributes(model);
        return "register";
    }

    @GetMapping("/app")
    public String app(Model model) {
        addEnvironmentAttributes(model);
        return "app";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        addEnvironmentAttributes(model);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(Model model) {
        addEnvironmentAttributes(model);
        return "reset-password";
    }
}
