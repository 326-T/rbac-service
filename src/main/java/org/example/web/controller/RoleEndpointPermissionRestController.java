package org.example.web.controller;

import org.example.persistence.entity.RoleEndpointPermission;
import org.example.service.RoleEndpointPermissionService;
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
@RequestMapping("/rbac-service/v1/roleEndpointPermissions")
public class RoleEndpointPermissionRestController {

  private final RoleEndpointPermissionService roleEndpointPermissionService;

  public RoleEndpointPermissionRestController(
      RoleEndpointPermissionService roleEndpointPermissionService) {
    this.roleEndpointPermissionService = roleEndpointPermissionService;
  }

  @GetMapping
  public Flux<RoleEndpointPermission> index() {
    return roleEndpointPermissionService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return roleEndpointPermissionService.count();
  }

  @GetMapping("/{id}")
  public Mono<RoleEndpointPermission> findById(@PathVariable Long id) {
    return roleEndpointPermissionService.findById(id);
  }

  @PostMapping
  public Mono<RoleEndpointPermission> save(
      @RequestBody RoleEndpointPermission roleEndpointPermission) {
    return roleEndpointPermissionService.insert(roleEndpointPermission);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return roleEndpointPermissionService.deleteById(id);
  }
}
