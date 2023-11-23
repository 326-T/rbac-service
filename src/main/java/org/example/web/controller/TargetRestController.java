package org.example.web.controller;

import org.example.persistence.entity.Target;
import org.example.service.TargetService;
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

  public TargetRestController(TargetService targetService) {
    this.targetService = targetService;
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
  public Mono<Target> save(@RequestBody Target target) {
    return targetService.insert(target);
  }

  @PutMapping("/{id}")
  public Mono<Target> update(@PathVariable Long id, @RequestBody Target target) {
    target.setId(id);
    return targetService.update(target);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return targetService.deleteById(id);
  }
}
