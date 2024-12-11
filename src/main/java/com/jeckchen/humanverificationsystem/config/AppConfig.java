package com.jeckchen.humanverificationsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${redirect.url}")
    private String redirectUrl;

    public String getRedirectUrl() {
        return redirectUrl;
    }
}