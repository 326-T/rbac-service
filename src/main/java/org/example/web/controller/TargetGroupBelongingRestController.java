package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.TargetGroupBelonging;
import org.example.service.ReactiveContextService;
import org.example.service.TargetGroupBelongingService;
import org.example.web.request.TargetGroupBelongingInsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/target-group-belongings")
public class TargetGroupBelongingRestController {

  private final TargetGroupBelongingService targetGroupBelongingService;
  private final ReactiveContextService reactiveContextService;

  public TargetGroupBelongingRestController(
      TargetGroupBelongingService targetGroupBelongingService, ReactiveContextService reactiveContextService) {
    this.targetGroupBelongingService = targetGroupBelongingService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<TargetGroupBelonging> index(@RequestParam("namespace-id") Long namespaceId) {
    return targetGroupBelongingService.findByNamespaceId(namespaceId);
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return targetGroupBelongingService.count();
  }

  @GetMapping("/{id}")
  public Mono<TargetGroupBelonging> findById(@PathVariable Long id) {
    return targetGroupBelongingService.findById(id);
  }

  @PostMapping
  public Mono<TargetGroupBelonging> save(
      @Valid @RequestBody TargetGroupBelongingInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          TargetGroupBelonging targetGroupBelonging = request.exportEntity();
          targetGroupBelonging.setCreatedBy(u.getId());
          return Mono.just(targetGroupBelonging);
        })
        .flatMap(targetGroupBelongingService::insert);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return targetGroupBelongingService.deleteById(id);
  }
}
