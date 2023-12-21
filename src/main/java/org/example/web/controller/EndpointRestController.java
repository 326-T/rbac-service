package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.dto.EndpointDetail;
import org.example.persistence.entity.Endpoint;
import org.example.service.EndpointDetailService;
import org.example.service.EndpointService;
import org.example.service.ReactiveContextService;
import org.example.web.request.EndpointInsertRequest;
import org.example.web.request.EndpointUpdateRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/endpoints")
public class EndpointRestController {

  private final EndpointService endpointService;
  private final EndpointDetailService endpointDetailService;
  private final ReactiveContextService reactiveContextService;

  public EndpointRestController(EndpointService endpointService, EndpointDetailService endpointDetailService,
      ReactiveContextService reactiveContextService) {
    this.endpointService = endpointService;
    this.endpointDetailService = endpointDetailService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<EndpointDetail> index(
      @RequestParam("namespace-id") Long namespaceId,
      @RequestParam(value = "role-id", required = false) Long roleId) {
    if (roleId == null) {
      return endpointDetailService.findByNamespaceId(namespaceId);
    }
    return endpointDetailService.findByNamespaceIdAndRoleId(namespaceId, roleId);
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return endpointService.count();
  }

  @GetMapping("/{id}")
  public Mono<Endpoint> findById(@PathVariable Long id) {
    return endpointService.findById(id);
  }

  @PostMapping
  public Mono<Endpoint> save(@Valid @RequestBody EndpointInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          Endpoint endpoint = request.exportEntity();
          endpoint.setCreatedBy(u.getId());
          return Mono.just(endpoint);
        })
        .flatMap(endpointService::insert);
  }

  @PutMapping("/{id}")
  public Mono<Endpoint> update(@PathVariable Long id, @Valid @RequestBody EndpointUpdateRequest request) {
    Endpoint endpoint = request.exportEntity();
    endpoint.setId(id);
    return endpointService.update(endpoint);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return endpointService.deleteById(id);
  }
}
