package org.example.web.controller;

import org.example.persistence.entity.RoleEndpointPermission;
import org.example.service.ReactiveContextService;
import org.example.service.RoleEndpointPermissionService;
import org.example.web.request.RoleEndpointPermissionInsertRequest;
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
@RequestMapping("/rbac-service/v1/role-endpoint-permissions")
public class RoleEndpointPermissionRestController {

  private final RoleEndpointPermissionService roleEndpointPermissionService;
  private final ReactiveContextService reactiveContextService;

  public RoleEndpointPermissionRestController(
      RoleEndpointPermissionService roleEndpointPermissionService, ReactiveContextService reactiveContextService) {
    this.roleEndpointPermissionService = roleEndpointPermissionService;
    this.reactiveContextService = reactiveContextService;
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
  public Mono<RoleEndpointPermission> save(@RequestBody RoleEndpointPermissionInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          RoleEndpointPermission roleEndpointPermission = request.exportEntity();
          roleEndpointPermission.setCreatedBy(u.getId());
          return Mono.just(roleEndpointPermission);
        })
        .flatMap(roleEndpointPermissionService::insert);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return roleEndpointPermissionService.deleteById(id);
  }
}
