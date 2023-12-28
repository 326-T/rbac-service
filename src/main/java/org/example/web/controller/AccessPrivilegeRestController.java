package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.dto.AccessPrivilege;
import org.example.service.AccessPrivilegeService;
import org.example.service.ReactiveContextService;
import org.example.util.constant.AccessPath;
import org.example.web.request.AccessPrivilegeRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(AccessPath.ACCESS_PRIVILEGES)
public class AccessPrivilegeRestController {

  private final AccessPrivilegeService accessPrivilegeService;
  private final ReactiveContextService reactiveContextService;

  public AccessPrivilegeRestController(AccessPrivilegeService accessPrivilegeService, ReactiveContextService reactiveContextService) {
    this.accessPrivilegeService = accessPrivilegeService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<AccessPrivilege> findByNamespace(@PathVariable("namespace-id") Long namespaceId) {
    return accessPrivilegeService.findByNamespace(namespaceId);
  }

  @PostMapping("/can-i")
  public Mono<Boolean> canAccess(
      ServerWebExchange exchange,
      @PathVariable("namespace-id") Long namespaceId,
      @Valid @RequestBody AccessPrivilegeRequest accessPrivilegeRequest) {
    return accessPrivilegeService.canAccess(
        reactiveContextService.extractCurrentUser(exchange).getId(),
        namespaceId,
        accessPrivilegeRequest);
  }
}
