package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Path;
import org.example.service.PathService;
import org.example.service.ReactiveContextService;
import org.example.util.constant.AccessPath;
import org.example.web.request.PathInsertRequest;
import org.example.web.request.PathUpdateRequest;
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
@RequestMapping(AccessPath.PATHS)
public class PathRestController {

  private final PathService pathService;
  private final ReactiveContextService reactiveContextService;

  public PathRestController(PathService pathService, ReactiveContextService reactiveContextService) {
    this.pathService = pathService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<Path> index(@PathVariable("namespace-id") Long namespaceId) {
    return pathService.findByNamespaceId(namespaceId);
  }

  @PostMapping
  public Mono<Path> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody PathInsertRequest request) {
    Path path = request.exportEntity();
    path.setNamespaceId(namespaceId);
    path.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return pathService.insert(path);
  }

  @PutMapping("/{id}")
  public Mono<Path> update(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id,
      @Valid @RequestBody PathUpdateRequest request) {
    Path path = request.exportEntity();
    path.setId(id);
    path.setNamespaceId(namespaceId);
    return pathService.update(path);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id) {
    return pathService.deleteById(id, namespaceId);
  }
}
