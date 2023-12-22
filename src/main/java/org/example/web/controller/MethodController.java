package org.example.web.controller;

import org.example.web.response.MethodResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/rbac-service/v1/methods")
public class MethodController {

  @GetMapping
  public Flux<MethodResponse> index() {
    return Flux.just(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS",
        "(POST|PUT|DELETE|PATCH)",
        "(GET|POST|PUT|DELETE|PATCH)"
    ).map(MethodResponse::new);
  }
}
