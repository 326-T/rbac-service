package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Target;
import org.example.service.ReactiveContextService;
import org.example.service.TargetService;
import org.example.util.constant.AccessPath;
import org.example.web.request.TargetInsertRequest;
import org.example.web.request.TargetUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.TARGETS)
public class TargetRestController {

  private final TargetService targetService;
  private final ReactiveContextService reactiveContextService;


  public TargetRestController(TargetService targetService, ReactiveContextService reactiveContextService) {
    this.targetService = targetService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<Target> index(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam(value = "target-group-id", required = false) Long targetGroupId) {
    if (targetGroupId == null) {
      return targetService.findByNamespaceId(namespaceId);
    }
    return targetService.findByNamespaceIdAndTargetGroupId(namespaceId, targetGroupId);
  }

  @PostMapping
  public Mono<Target> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody TargetInsertRequest request) {
    Target target = request.exportEntity();
    target.setNamespaceId(namespaceId);
    target.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return targetService.insert(target);
  }

  @PutMapping("/{id}")
  public Mono<Target> update(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id,
      @Valid @RequestBody TargetUpdateRequest request) {
    Target target = request.exportEntity();
    target.setId(id);
    target.setNamespaceId(namespaceId);
    return targetService.update(target);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id) {
    return targetService.deleteById(id, namespaceId);
  }
}
