package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Namespace;
import org.example.service.NamespaceService;
import org.example.service.ReactiveContextService;
import org.example.service.SystemRoleService;
import org.example.web.request.NamespaceInsertRequest;
import org.example.web.request.NamespaceUpdateRequest;
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
  private final SystemRoleService systemRoleService;
  private final ReactiveContextService reactiveContextService;

  public NamespaceRestController(NamespaceService namespaceService, SystemRoleService systemRoleService,
      ReactiveContextService reactiveContextService) {
    this.namespaceService = namespaceService;
    this.systemRoleService = systemRoleService;
    this.reactiveContextService = reactiveContextService;
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
  public Mono<Namespace> save(@Valid @RequestBody NamespaceInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          Namespace namespace = request.exportEntity();
          namespace.setCreatedBy(u.getId());
          return Mono.just(namespace);
        })
        .flatMap(namespaceService::insert)
        .flatMap(namespace ->
            systemRoleService.createSystemRole(namespace, namespace.getCreatedBy())
                .thenReturn(namespace)
        );
  }

  @PutMapping("/{id}")
  public Mono<Namespace> update(@PathVariable Long id, @Valid @RequestBody NamespaceUpdateRequest request) {
    Namespace namespace = request.exportEntity();
    namespace.setId(id);
    return namespaceService.update(namespace);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return namespaceService.deleteById(id);
  }
}
