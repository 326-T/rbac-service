package org.example.web.controller;

import org.example.persistence.entity.SystemRole;
import org.example.service.SystemRoleService;
import org.example.util.constant.AccessPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(AccessPath.SYSTEM_ROLES)
public class SystemRoleRestController {

  private final SystemRoleService systemRoleService;

  public SystemRoleRestController(SystemRoleService systemRoleService) {
    this.systemRoleService = systemRoleService;
  }

  @GetMapping
  public Flux<SystemRole> findByNamespaceId(@PathVariable("namespace-id") Long namespaceId) {
    return systemRoleService.findByNamespaceId(namespaceId);
  }
}
