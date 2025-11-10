package eu.robm15.tenxdevs.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
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
