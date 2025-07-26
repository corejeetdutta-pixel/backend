package com.recruitment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        // Set cookie name (defaults to SESSION)
        serializer.setCookieName("JSESSIONID");
        
        // Set cookie path to root
        serializer.setCookiePath("/");
        
        // Allow cross-site cookies
        serializer.setSameSite("None");
        
        // Enable secure cookies (requires HTTPS)
        serializer.setUseSecureCookie(true);
        
        // Make cookie accessible to JavaScript if needed (set to false for security)
        serializer.setUseHttpOnlyCookie(true);
        
        // Set domain name pattern (use your actual domain)
        // serializer.setDomainName(".atract.in");
        
        return serializer;
    }

    // Optional: Configure session timeout (in seconds)
    // @Bean
    // public ConfigureRedisAction configureRedisAction() {
    //     return ConfigureRedisAction.NO_OP;
    // }
}
