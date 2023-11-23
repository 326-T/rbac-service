package org.example.web.controller;

import org.example.persistence.entity.Endpoint;
import org.example.service.EndpointService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/endpoints")
public class EndpointRestController {

  private final EndpointService endpointService;

  public EndpointRestController(EndpointService endpointService) {
    this.endpointService = endpointService;
  }

  @GetMapping
  public Flux<Endpoint> index() {
    return endpointService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return endpointService.count();
  }

  @GetMapping("/{id}")
  public Mono<Endpoint> findById(@PathVariable Long id) {
    return endpointService.findById(id);
  }

  @PostMapping
  public Mono<Endpoint> save(@RequestBody Endpoint endpoint) {
    return endpointService.insert(endpoint);
  }

  @PutMapping("/{id}")
  public Mono<Endpoint> update(@PathVariable Long id, @RequestBody Endpoint endpoint) {
    endpoint.setId(id);
    return endpointService.update(endpoint);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return endpointService.deleteById(id);
  }
}
