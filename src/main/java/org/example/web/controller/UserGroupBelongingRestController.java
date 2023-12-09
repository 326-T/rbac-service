package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupBelongingService;
import org.example.web.request.UserGroupBelongingInsertRequest;
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
@RequestMapping("/rbac-service/v1/user-group-belongings")
public class UserGroupBelongingRestController {

  private final UserGroupBelongingService userGroupBelongingService;
  private final ReactiveContextService reactiveContextService;

  public UserGroupBelongingRestController(
      UserGroupBelongingService userGroupBelongingService, ReactiveContextService reactiveContextService) {
    this.userGroupBelongingService = userGroupBelongingService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<UserGroupBelonging> index() {
    return userGroupBelongingService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userGroupBelongingService.count();
  }

  @GetMapping("/{id}")
  public Mono<UserGroupBelonging> findById(@PathVariable Long id) {
    return userGroupBelongingService.findById(id);
  }

  @PostMapping
  public Mono<UserGroupBelonging> save(@Valid @RequestBody UserGroupBelongingInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          UserGroupBelonging userGroupBelonging = request.exportEntity();
          userGroupBelonging.setCreatedBy(u.getId());
          return Mono.just(userGroupBelonging);
        })
        .flatMap(userGroupBelongingService::insert);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userGroupBelongingService.deleteById(id);
  }
}
