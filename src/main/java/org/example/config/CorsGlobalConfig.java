package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class CorsGlobalConfig implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(org.springframework.web.reactive.config.CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns("http://localhost:*")
        .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name())
        .allowedHeaders("*");
  }
}
