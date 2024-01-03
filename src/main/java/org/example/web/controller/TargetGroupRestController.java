package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.TargetGroup;
import org.example.service.ReactiveContextService;
import org.example.service.TargetGroupService;
import org.example.util.constant.AccessPath;
import org.example.web.request.TargetGroupInsertRequest;
import org.example.web.request.TargetGroupUpdateRequest;
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
@RequestMapping(AccessPath.TARGET_GROUPS)
public class TargetGroupRestController {

  private final TargetGroupService targetGroupService;

  private final ReactiveContextService reactiveContextService;

  public TargetGroupRestController(TargetGroupService targetGroupService, ReactiveContextService reactiveContextService) {
    this.targetGroupService = targetGroupService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<TargetGroup> index(@PathVariable("namespace-id") Long namespaceId) {
    return targetGroupService.findByNamespaceId(namespaceId);
  }

  @PostMapping
  public Mono<TargetGroup> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody TargetGroupInsertRequest request) {
    TargetGroup targetGroup = request.exportEntity();
    targetGroup.setNamespaceId(namespaceId);
    targetGroup.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return targetGroupService.insert(targetGroup);
  }

  @PutMapping("/{id}")
  public Mono<TargetGroup> update(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id,
      @Valid @RequestBody TargetGroupUpdateRequest request) {
    TargetGroup targetGroup = request.exportEntity();
    targetGroup.setId(id);
    return targetGroupService.update(targetGroup, namespaceId);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id) {
    return targetGroupService.deleteById(id, namespaceId);
  }
}
