package org.example.web.controller;

import jakarta.validation.Valid;
import org.example.persistence.entity.Path;
import org.example.service.PathService;
import org.example.service.ReactiveContextService;
import org.example.web.request.PathInsertRequest;
import org.example.web.request.PathUpdateRequest;
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
@RequestMapping("/rbac-service/v1/paths")
public class PathRestController {

  private final PathService pathService;
  private final ReactiveContextService reactiveContextService;

  public PathRestController(PathService pathService, ReactiveContextService reactiveContextService) {
    this.pathService = pathService;
    this.reactiveContextService = reactiveContextService;
  }

  @GetMapping
  public Flux<Path> index() {
    return pathService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return pathService.count();
  }

  @GetMapping("/{id}")
  public Mono<Path> findById(@PathVariable Long id) {
    return pathService.findById(id);
  }

  @PostMapping
  public Mono<Path> save(@Valid @RequestBody PathInsertRequest request) {
    return reactiveContextService.getCurrentUser()
        .flatMap(u -> {
          Path path = request.exportEntity();
          path.setCreatedBy(u.getId());
          return Mono.just(path);
        })
        .flatMap(pathService::insert);
  }

  @PutMapping("/{id}")
  public Mono<Path> update(@PathVariable Long id, @Valid @RequestBody PathUpdateRequest request) {
    Path path = request.exportEntity();
    path.setId(id);
    return pathService.update(path);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return pathService.deleteById(id);
  }
}
