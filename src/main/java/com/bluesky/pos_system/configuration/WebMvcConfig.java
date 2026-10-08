package com.bluesky.pos_system.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CDN & Static Assets Caching: Static JS, CSS, fonts, and images cached for 1 year with immutable flag
        registry.addResourceHandler("/static/**", "/assets/**", "/resources/**")
                .addResourceLocations("classpath:/static/", "classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

        // Uploaded Media & Product Images: Cached for 30 days
        registry.addResourceHandler("/uploads/**", "/images/**")
                .addResourceLocations("file:uploads/", "classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
