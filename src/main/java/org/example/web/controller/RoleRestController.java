package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Role;
import org.example.service.ReactiveContextService;
import org.example.service.RoleService;
import org.example.util.constant.AccessPath;
import org.example.web.request.RoleInsertRequest;
import org.example.web.request.RoleUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.ROLES)
public class RoleRestController {

  private final RoleService roleService;

  private final ReactiveContextService reactiveContextService;

  public RoleRestController(RoleService roleService, ReactiveContextService reactiveContextService) {
    this.roleService = roleService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<Role> index(
      @PathVariable("namespace-id") Long namespaceId,
      @RequestParam(value = "user-group-id", required = false) Long userGroupId) {
    if (userGroupId == null) {
      return roleService.findByNamespaceId(namespaceId);
    }
    return roleService.findByNamespaceIdAndUserGroupId(namespaceId, userGroupId);
  }

  @PostMapping
  public Mono<Role> save(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody RoleInsertRequest request) {
    Role role = request.exportEntity();
    role.setNamespaceId(namespaceId);
    role.setCreatedBy(reactiveContextService.extractCurrentUser(exchange).getId());
    return roleService.insert(role);
  }

  @PutMapping("/{id}")
  public Mono<Role> update(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id,
      @Valid @RequestBody RoleUpdateRequest request) {
    Role role = request.exportEntity();
    role.setId(id);
    role.setNamespaceId(namespaceId);
    return roleService.update(role);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(
      @PathVariable("namespace-id") Long namespaceId,
      @PathVariable Long id) {
    return roleService.deleteById(id, namespaceId);
  }
}
