package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupBelongingService;
import org.example.util.constant.AccessPath;
import org.example.web.request.UserGroupBelongingInsertRequest;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.USER_GROUP_BELONGINGS)
public class UserGroupBelongingRestController {

  private final UserGroupBelongingService userGroupBelongingService;
  private final ReactiveContextService reactiveContextService;

  public UserGroupBelongingRestController(
      UserGroupBelongingService userGroupBelongingService, ReactiveContextService reactiveContextService) {
    this.userGroupBelongingService = userGroupBelongingService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<UserGroupBelonging> index(@PathVariable("namespace-id") Long namespaceId) {
    return userGroupBelongingService.findByNamespaceId(namespaceId);
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
  public Mono<UserGroupBelonging> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody UserGroupBelongingInsertRequest request) {
    UserGroupBelonging userGroupBelonging = request.exportEntity();
    userGroupBelonging.setNamespaceId(namespaceId);
    userGroupBelonging.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return userGroupBelongingService.insert(userGroupBelonging);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userGroupBelongingService.deleteById(id);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteByUniqueKeys(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam("user-id") Long userId,
      @RequestParam("user-group-id") Long userGroupId) {
    return userGroupBelongingService.deleteByUniqueKeys(namespaceId, userId, userGroupId);
  }
}
