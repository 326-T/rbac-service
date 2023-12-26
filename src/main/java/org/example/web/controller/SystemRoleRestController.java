package org.example.web.controller;

import org.example.persistence.entity.SystemRole;
import org.example.service.SystemRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/rbac-service/v1/system-roles")
public class SystemRoleRestController {

  private final SystemRoleService systemRoleService;

  public SystemRoleRestController(SystemRoleService systemRoleService) {
    this.systemRoleService = systemRoleService;
  }

  @GetMapping
  public Flux<SystemRole> findByNamespaceId(@RequestParam("namespace-id") Long namespaceId) {
    return systemRoleService.findByNamespaceId(namespaceId);
  }
}
