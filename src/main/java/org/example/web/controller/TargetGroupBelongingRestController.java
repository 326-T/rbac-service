package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.TargetGroupBelonging;
import org.example.service.ReactiveContextService;
import org.example.service.TargetGroupBelongingService;
import org.example.util.constant.AccessPath;
import org.example.web.request.TargetGroupBelongingInsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.TARGET_GROUP_BELONGINGS)
public class TargetGroupBelongingRestController {

  private final TargetGroupBelongingService targetGroupBelongingService;
  private final ReactiveContextService reactiveContextService;

  public TargetGroupBelongingRestController(
      TargetGroupBelongingService targetGroupBelongingService, ReactiveContextService reactiveContextService) {
    this.targetGroupBelongingService = targetGroupBelongingService;
    this.reactiveContextService = reactiveContextService;
  }

  @PostMapping
  public Mono<TargetGroupBelonging> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody TargetGroupBelongingInsertRequest request) {
    TargetGroupBelonging targetGroupBelonging = request.exportEntity();
    targetGroupBelonging.setNamespaceId(namespaceId);
    targetGroupBelonging.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return targetGroupBelongingService.insert(targetGroupBelonging);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteByUniqueKeys(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam("target-id") Long targetId,
      @RequestParam("target-group-id") Long targetGroupId) {
    return targetGroupBelongingService.deleteByUniqueKeys(namespaceId, targetId, targetGroupId);
  }
}
