package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.UserGroup;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupService;
import org.example.util.constant.AccessPath;
import org.example.web.request.UserGroupInsertRequest;
import org.example.web.request.UserGroupUpdateRequest;
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
@RequestMapping(AccessPath.USER_GROUPS)
public class UserGroupRestController {

  private final UserGroupService userGroupService;
  private final ReactiveContextService reactiveContextService;

  public UserGroupRestController(UserGroupService userGroupService, ReactiveContextService reactiveContextService) {
    this.userGroupService = userGroupService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<UserGroup> index(@PathVariable("namespace-id") Long namespaceId) {
    return userGroupService.findByNamespaceId(namespaceId);
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userGroupService.count();
  }

  @GetMapping("/{id}")
  public Mono<UserGroup> findById(@PathVariable Long id) {
    return userGroupService.findById(id);
  }

  @PostMapping
  public Mono<UserGroup> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody UserGroupInsertRequest request) {
    UserGroup userGroup = request.exportEntity();
    userGroup.setNamespaceId(namespaceId);
    userGroup.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return userGroupService.insert(userGroup);
  }

  @PutMapping("/{id}")
  public Mono<UserGroup> update(@PathVariable Long id, @Valid @RequestBody UserGroupUpdateRequest request) {
    UserGroup userGroup = request.exportEntity();
    userGroup.setId(id);
    return userGroupService.update(userGroup);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userGroupService.deleteById(id);
  }
}
