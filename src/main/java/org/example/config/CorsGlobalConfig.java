package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfig {

  @Bean
  CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.addAllowedOriginPattern("http://localhost:*");
    corsConfig.addAllowedMethod(HttpMethod.GET);
    corsConfig.addAllowedMethod(HttpMethod.POST);
    corsConfig.addAllowedMethod(HttpMethod.PUT);
    corsConfig.addAllowedMethod(HttpMethod.DELETE);
    corsConfig.addAllowedMethod(HttpMethod.OPTIONS);
    corsConfig.addAllowedHeader("*");

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }
}
