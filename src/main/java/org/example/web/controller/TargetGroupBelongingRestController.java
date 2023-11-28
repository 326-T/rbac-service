package org.example.web.controller;

import org.example.persistence.entity.TargetGroupBelonging;
import org.example.service.TargetGroupBelongingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/target-group-belongings")
public class TargetGroupBelongingRestController {

  private final TargetGroupBelongingService targetGroupBelongingService;

  public TargetGroupBelongingRestController(
      TargetGroupBelongingService targetGroupBelongingService) {
    this.targetGroupBelongingService = targetGroupBelongingService;
  }

  @GetMapping
  public Flux<TargetGroupBelonging> index() {
    return targetGroupBelongingService.findAll();
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
      @RequestBody TargetGroupBelonging targetGroupBelonging) {
    return targetGroupBelongingService.insert(targetGroupBelonging);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return targetGroupBelongingService.deleteById(id);
  }
}
