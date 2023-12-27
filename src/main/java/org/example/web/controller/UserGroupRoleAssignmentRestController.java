package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupRoleAssignmentService;
import org.example.util.constant.AccessPath;
import org.example.web.request.UserGroupRoleAssignmentInsertRequest;
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
@RequestMapping(AccessPath.USER_GROUP_ROLE_ASSIGNMENTS)
public class UserGroupRoleAssignmentRestController {

  private final UserGroupRoleAssignmentService userGroupRoleAssignmentService;
  private final ReactiveContextService reactiveContextService;

  public UserGroupRoleAssignmentRestController(
      UserGroupRoleAssignmentService userGroupRoleAssignmentService, ReactiveContextService reactiveContextService) {
    this.userGroupRoleAssignmentService = userGroupRoleAssignmentService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<UserGroupRoleAssignment> index(@PathVariable("namespace-id") Long namespaceId) {
    return userGroupRoleAssignmentService.findByNamespaceId(namespaceId);
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return userGroupRoleAssignmentService.count();
  }

  @GetMapping("/{id}")
  public Mono<UserGroupRoleAssignment> findById(@PathVariable Long id) {
    return userGroupRoleAssignmentService.findById(id);
  }

  @PostMapping
  public Mono<UserGroupRoleAssignment> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody UserGroupRoleAssignmentInsertRequest request) {
    UserGroupRoleAssignment userGroupRoleAssignment = request.exportEntity();
    userGroupRoleAssignment.setNamespaceId(namespaceId);
    userGroupRoleAssignment.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return userGroupRoleAssignmentService.insert(userGroupRoleAssignment);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userGroupRoleAssignmentService.deleteById(id);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteByUniqueKeys(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam("user-group-id") Long userGroupId,
      @RequestParam("role-id") Long roleId) {
    return userGroupRoleAssignmentService.deleteByUniqueKeys(namespaceId, userGroupId, roleId);
  }
}
