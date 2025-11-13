package eu.robm15.tenxdevs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!localh2") // Not active for localh2 profile
@ConfigurationProperties(prefix = "supabase")
public class SupabaseConfigProperties {

    private String url;
    private String anonKey;
    private String jwtSecret;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAnonKey() {
        return anonKey;
    }

    public void setAnonKey(String anonKey) {
        this.anonKey = anonKey;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
}
