package org.example.web.controller;

import org.example.persistence.entity.Namespace;
import org.example.service.NamespaceService;
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
@RequestMapping("/rbac-service/v1/namespaces")
public class NamespaceRestController {

  private final NamespaceService namespaceService;

  public NamespaceRestController(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @GetMapping
  public Flux<Namespace> index() {
    return namespaceService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return namespaceService.count();
  }

  @GetMapping("/{id}")
  public Mono<Namespace> findById(@PathVariable Long id) {
    return namespaceService.findById(id);
  }

  @PostMapping
  public Mono<Namespace> save(@RequestBody Namespace namespace) {
    return namespaceService.insert(namespace);
  }

  @PutMapping("/{id}")
  public Mono<Namespace> update(@PathVariable Long id, @RequestBody Namespace namespace) {
    namespace.setId(id);
    return namespaceService.update(namespace);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return namespaceService.deleteById(id);
  }
}