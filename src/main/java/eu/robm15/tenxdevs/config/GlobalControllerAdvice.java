package eu.robm15.tenxdevs.config;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides Supabase configuration to all Thymeleaf templates for profiles with authentication
 */
@ControllerAdvice
@Profile("!localh2") // Not active for localh2 profile - no Supabase needed
public class GlobalControllerAdvice {

    private final SupabaseConfigProperties supabaseConfig;

    public GlobalControllerAdvice(SupabaseConfigProperties supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
    }

    /**
     * Makes Supabase URL available to all Thymeleaf templates
     */
    @ModelAttribute("supabaseUrl")
    public String supabaseUrl() {
        return supabaseConfig.getUrl();
    }

    /**
     * Makes Supabase anon key available to all Thymeleaf templates
     */
    @ModelAttribute("supabaseAnonKey")
    public String supabaseAnonKey() {
        return supabaseConfig.getAnonKey();
    }
}

/**
 * Provides dummy Supabase configuration for localh2 profile (no authentication)
 */
@ControllerAdvice
@Profile("localh2")
class LocalH2ControllerAdvice {

    @ModelAttribute("supabaseUrl")
    public String supabaseUrl() {
        return ""; // No Supabase in localh2 mode
    }

    @ModelAttribute("supabaseAnonKey")
    public String supabaseAnonKey() {
        return ""; // No Supabase in localh2 mode
    }
}
