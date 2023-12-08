package org.example.web.controller;

import org.example.persistence.dto.AccessPrivilege;
import org.example.service.AccessPrivilegeService;
import org.example.service.ReactiveContextService;
import org.example.web.request.AccessPrivilegeRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/access-privileges")
public class AccessPrivilegeRestController {

  private final AccessPrivilegeService accessPrivilegeService;
  private final ReactiveContextService reactiveContextService;

  public AccessPrivilegeRestController(AccessPrivilegeService accessPrivilegeService, ReactiveContextService reactiveContextService) {
    this.accessPrivilegeService = accessPrivilegeService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<AccessPrivilege> findByNamespace(@RequestParam("namespace-id") Long namespaceId) {
    return accessPrivilegeService.findByNamespace(namespaceId);
  }

  @PostMapping("/can-i")
  public Mono<Boolean> canAccess(@RequestBody AccessPrivilegeRequest accessPrivilegeRequest) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> accessPrivilegeService.canAccess(u.getId(), accessPrivilegeRequest));
  }
}
