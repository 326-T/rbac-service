package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.RoleEndpointPermission;
import org.example.service.ReactiveContextService;
import org.example.service.RoleEndpointPermissionService;
import org.example.util.constant.AccessPath;
import org.example.web.request.RoleEndpointPermissionInsertRequest;
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
@RequestMapping(AccessPath.ROLE_ENDPOINT_PERMISSIONS)
public class RoleEndpointPermissionRestController {

  private final RoleEndpointPermissionService roleEndpointPermissionService;
  private final ReactiveContextService reactiveContextService;

  public RoleEndpointPermissionRestController(
      RoleEndpointPermissionService roleEndpointPermissionService, ReactiveContextService reactiveContextService) {
    this.roleEndpointPermissionService = roleEndpointPermissionService;
    this.reactiveContextService = reactiveContextService;
  }

  @PostMapping
  public Mono<RoleEndpointPermission> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody RoleEndpointPermissionInsertRequest request) {
    RoleEndpointPermission roleEndpointPermission = request.exportEntity();
    roleEndpointPermission.setNamespaceId(namespaceId);
    roleEndpointPermission.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return roleEndpointPermissionService.insert(roleEndpointPermission);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteByUniqueKeys(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam("role-id") Long roleId,
      @RequestParam("endpoint-id") Long endpointId) {
    return roleEndpointPermissionService.deleteByUniqueKeys(namespaceId, roleId, endpointId);
  }
}
