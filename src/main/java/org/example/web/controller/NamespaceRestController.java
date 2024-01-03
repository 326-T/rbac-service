package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Namespace;
import org.example.service.NamespaceService;
import org.example.service.ReactiveContextService;
import org.example.service.SystemRoleService;
import org.example.util.constant.AccessPath;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.NAMESPACES)
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

  @PostMapping
  public Mono<Namespace> save(
      ServerWebExchange exchange,
      @Valid @RequestBody NamespaceInsertRequest request) {
    Namespace namespace = request.exportEntity();
    namespace.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return namespaceService.insert(namespace)
        .flatMap(n -> systemRoleService.createSystemRole(n, n.getCreatedBy()).thenReturn(n));
  }

  @PutMapping("/{id}")
  public Mono<Namespace> update(
      ServerWebExchange exchange,
      @PathVariable Long id,
      @Valid @RequestBody NamespaceUpdateRequest request) {
    Namespace namespace = request.exportEntity();
    namespace.setId(id);
    return namespaceService.update(namespace,
        reactiveContextService.extractCurrentUser(exchange).getId());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(
      ServerWebExchange exchange,
      @PathVariable Long id) {
    return namespaceService.deleteById(id,
        reactiveContextService.extractCurrentUser(exchange).getId());
  }
}
