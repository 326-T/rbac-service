package org.example.web.controller;

import org.example.persistence.entity.TargetClusterBelonging;
import org.example.service.TargetClusterBelongingService;
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
@RequestMapping("/rbac-service/v1/targetClusterBelongings")
public class TargetClusterBelongingRestController {

  private final TargetClusterBelongingService targetClusterBelongingService;

  public TargetClusterBelongingRestController(
      TargetClusterBelongingService targetClusterBelongingService) {
    this.targetClusterBelongingService = targetClusterBelongingService;
  }

  @GetMapping
  public Flux<TargetClusterBelonging> index() {
    return targetClusterBelongingService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return targetClusterBelongingService.count();
  }

  @GetMapping("/{id}")
  public Mono<TargetClusterBelonging> findById(@PathVariable Long id) {
    return targetClusterBelongingService.findById(id);
  }

  @PostMapping
  public Mono<TargetClusterBelonging> save(
      @RequestBody TargetClusterBelonging targetClusterBelonging) {
    return targetClusterBelongingService.insert(targetClusterBelonging);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return targetClusterBelongingService.deleteById(id);
  }
}
