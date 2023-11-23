package org.example.web.controller;

import org.example.persistence.entity.Path;
import org.example.service.PathService;
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

  public PathRestController(PathService pathService) {
    this.pathService = pathService;
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
  public Mono<Path> save(@RequestBody Path path) {
    return pathService.insert(path);
  }

  @PutMapping("/{id}")
  public Mono<Path> update(@PathVariable Long id, @RequestBody Path path) {
    path.setId(id);
    return pathService.update(path);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return pathService.deleteById(id);
  }
}
