package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.service.ReactiveContextService;
import org.example.service.UserSystemRolePermissionService;
import org.example.web.request.UserSystemRolePermissionInsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/user-system-role-permissions")
public class UserSystemRolePermissionRestController {

  private final UserSystemRolePermissionService userSystemRolePermissionService;
  private final ReactiveContextService reactiveContextService;

  public UserSystemRolePermissionRestController(UserSystemRolePermissionService userSystemRolePermissionService,
      ReactiveContextService reactiveContextService) {
    this.userSystemRolePermissionService = userSystemRolePermissionService;
    this.reactiveContextService = reactiveContextService;
  }

  @PostMapping
  public Mono<UserSystemRolePermission> save(@Valid @RequestBody UserSystemRolePermissionInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          UserSystemRolePermission userSystemRolePermission = request.exportEntity();
          userSystemRolePermission.setCreatedBy(u.getId());
          return Mono.just(userSystemRolePermission);
        })
        .flatMap(userSystemRolePermissionService::insert);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteByUniqueKeys(
      @RequestParam("user-id") Long userId,
      @RequestParam("system-role-id") Long systemRoleId) {
    return userSystemRolePermissionService.deleteByUniqueKeys(userId, systemRoleId);
  }
}
