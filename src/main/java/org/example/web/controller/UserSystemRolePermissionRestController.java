package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.service.ReactiveContextService;
import org.example.service.UserSystemRolePermissionService;
import org.example.util.constant.AccessPath;
import org.example.web.request.UserSystemRolePermissionInsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.USER_SYSTEM_ROLE_PERMISSIONS)
public class UserSystemRolePermissionRestController {

  private final UserSystemRolePermissionService userSystemRolePermissionService;
  private final ReactiveContextService reactiveContextService;

  public UserSystemRolePermissionRestController(UserSystemRolePermissionService userSystemRolePermissionService,
      ReactiveContextService reactiveContextService) {
    this.userSystemRolePermissionService = userSystemRolePermissionService;
    this.reactiveContextService = reactiveContextService;
  }

  @PostMapping
  public Mono<UserSystemRolePermission> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody UserSystemRolePermissionInsertRequest request) {
    UserSystemRolePermission userSystemRolePermission = request.exportEntity();
    userSystemRolePermission.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return userSystemRolePermissionService.insert(userSystemRolePermission, namespaceId);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteByUniqueKeys(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam("user-id") Long userId,
      @RequestParam("system-role-id") Long systemRoleId) {
    return userSystemRolePermissionService.deleteByUniqueKeys(userId, systemRoleId, namespaceId);
  }
}
