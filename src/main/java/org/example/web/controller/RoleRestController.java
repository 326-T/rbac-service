package org.example.web.controller;

import org.example.persistence.entity.Role;
import org.example.service.RoleService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/roles")
public class RoleRestController {

  private final RoleService roleService;

  public RoleRestController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping
  public Flux<Role> index() {
    return roleService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return roleService.count();
  }

  @GetMapping("/{id}")
  public Mono<Role> findById(@PathVariable Long id) {
    return roleService.findById(id);
  }

  @PostMapping
  public Mono<Role> save(@RequestBody Role role) {
    return roleService.insert(role);
  }

  @PutMapping("/{id}")
  public Mono<Role> update(@PathVariable Long id, @RequestBody Role role) {
    role.setId(id);
    return roleService.update(role);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return roleService.deleteById(id);
  }
}
