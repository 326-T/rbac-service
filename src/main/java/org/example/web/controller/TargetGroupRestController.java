package org.example.web.controller;

import org.example.persistence.entity.TargetGroup;
import org.example.service.TargetGroupService;
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
@RequestMapping("/rbac-service/v1/target-groups")
public class TargetGroupRestController {

  private final TargetGroupService targetGroupService;

  public TargetGroupRestController(TargetGroupService targetGroupService) {
    this.targetGroupService = targetGroupService;
  }

  @GetMapping
  public Flux<TargetGroup> index() {
    return targetGroupService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return targetGroupService.count();
  }

  @GetMapping("/{id}")
  public Mono<TargetGroup> findById(@PathVariable Long id) {
    return targetGroupService.findById(id);
  }

  @PostMapping
  public Mono<TargetGroup> save(@RequestBody TargetGroup targetGroup) {
    return targetGroupService.insert(targetGroup);
  }

  @PutMapping("/{id}")
  public Mono<TargetGroup> update(@PathVariable Long id, @RequestBody TargetGroup targetGroup) {
    targetGroup.setId(id);
    return targetGroupService.update(targetGroup);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return targetGroupService.deleteById(id);
  }
}
