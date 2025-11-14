package eu.robm15.tenxdevs.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "spring.ai.openai")
public class AiConfigProperties {

    @NotBlank(message = "AI API key is required")
    private String apiKey;

    private ChatOptions chat = new ChatOptions();

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ChatOptions getChat() {
        return chat;
    }

    public void setChat(ChatOptions chat) {
        this.chat = chat;
    }

    public static class ChatOptions {
        private OptionsConfig options = new OptionsConfig();

        public OptionsConfig getOptions() {
            return options;
        }

        public void setOptions(OptionsConfig options) {
            this.options = options;
        }
    }

    public static class OptionsConfig {
        @NotBlank(message = "AI model is required")
        private String model;

        @NotNull(message = "AI temperature is required")
        @DecimalMin(value = "0.0", message = "Temperature must be between 0.0 and 2.0")
        @DecimalMax(value = "2.0", message = "Temperature must be between 0.0 and 2.0")
        private Double temperature;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }
}
