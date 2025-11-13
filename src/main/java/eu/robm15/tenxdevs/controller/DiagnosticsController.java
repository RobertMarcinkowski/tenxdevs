package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.config.SupabaseConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Diagnostics controller for checking configuration
 * Only active in non-production profiles for security
 */
@RestController
@Profile("!prod")
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private SupabaseConfigProperties supabaseConfig;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();

        String[] activeProfiles = environment.getActiveProfiles();
        config.put("activeProfile", activeProfiles.length > 0 ? activeProfiles[0] : "default");
        config.put("hasSupabaseConfig", supabaseConfig != null);

        if (supabaseConfig != null) {
            config.put("supabaseUrl", supabaseConfig.getUrl());
            config.put("supabaseAnonKeyPresent", supabaseConfig.getAnonKey() != null && !supabaseConfig.getAnonKey().isEmpty());
            config.put("supabaseAnonKeyLength", supabaseConfig.getAnonKey() != null ? supabaseConfig.getAnonKey().length() : 0);
            config.put("jwtSecretPresent", supabaseConfig.getJwtSecret() != null && !supabaseConfig.getJwtSecret().isEmpty());
        }

        return config;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("profile", environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default");
        return health;
    }
}
