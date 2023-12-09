package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Target;
import org.example.service.ReactiveContextService;
import org.example.service.TargetService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/targets")
public class TargetRestController {

  private final TargetService targetService;
  private final ReactiveContextService reactiveContextService;


  public TargetRestController(TargetService targetService, ReactiveContextService reactiveContextService) {
    this.targetService = targetService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<Target> index() {
    return targetService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return targetService.count();
  }

  @GetMapping("/{id}")
  public Mono<Target> findById(@PathVariable Long id) {
    return targetService.findById(id);
  }

  @PostMapping
  public Mono<Target> save(@Valid @RequestBody TargetInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          Target target = request.exportEntity();
          target.setCreatedBy(u.getId());
          return Mono.just(target);
        })
        .flatMap(targetService::insert);
  }

  @PutMapping("/{id}")
  public Mono<Target> update(@PathVariable Long id, @Valid @RequestBody TargetUpdateRequest request) {
    Target target = request.exportEntity();
    target.setId(id);
    return targetService.update(target);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return targetService.deleteById(id);
  }
}
