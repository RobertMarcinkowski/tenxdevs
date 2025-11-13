package eu.robm15.tenxdevs.controller;

import eu.robm15.tenxdevs.config.SupabaseConfigProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Profile("!localh2") // Not active for localh2 profile - no Supabase config needed
@RequestMapping("/api/config")
public class ConfigController {

    private final SupabaseConfigProperties supabaseConfig;

    public ConfigController(SupabaseConfigProperties supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
    }

    /**
     * Public endpoint that exposes Supabase client configuration
     * Only exposes public/safe values (URL and anon key)
     */
    @GetMapping("/supabase")
    public Map<String, String> getSupabaseConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("url", supabaseConfig.getUrl());
        config.put("anonKey", supabaseConfig.getAnonKey());
        // Note: DO NOT expose jwtSecret - it's for backend validation only
        return config;
    }
}
