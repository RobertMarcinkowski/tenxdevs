package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.model.Experiment;
import eu.robm15.tenxdevs.repository.ExperimentRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenxdevsController {

    @Autowired
    private OpenAiChatModel openAiChatModel;

    @Autowired
    private ExperimentRepository experimentRepository;

    @GetMapping("/api/status")
    public String status() {
        return "Tenxdevs API v 01";
    }

    @GetMapping("/tenxdevs")
    public String tenxdevs(@RequestParam(value = "name", defaultValue = "World") String name) {
        var exp = new Experiment();
        exp.setName(name);
        experimentRepository.save(exp);
        return String.format("Docker test 11. Hello %s!", name);
    }

    @GetMapping("/tenxdevs-ask-ai")
    public String tenxdevsAskAi(@RequestParam(value = "prompt", defaultValue = "What is the capital of Poland?") String prompt) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userLogin = authentication != null && authentication.isAuthenticated()
            ? authentication.getName()
            : "anonymous";

        // Get AI model configuration
        var chatOptions = openAiChatModel.getDefaultOptions();
        String modelName = chatOptions != null && chatOptions.getModel() != null
            ? chatOptions.getModel()
            : "unknown";
        Double temperature = chatOptions != null && chatOptions.getTemperature() != null
            ? chatOptions.getTemperature()
            : null;

        // Build response with all information
        StringBuilder response = new StringBuilder();
        response.append("User: ").append(userLogin).append("\n");
        response.append("AI Model: ").append(modelName).append("\n");
        response.append("Temperature: ").append(temperature != null ? temperature : "default").append("\n");
        response.append("Prompt: ").append(prompt).append("\n");

        try {
            // Call AI
            var answer = openAiChatModel.call(prompt);
            response.append("Response: ").append(answer);
        } catch (Exception e) {
            response.append("Error: Failed to call AI - ").append(e.getMessage());
        }

        return response.toString();
    }

}
