package org.example.web.controller;

import org.example.persistence.entity.Cluster;
import org.example.service.ClusterService;
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
@RequestMapping("/rbac-service/v1/clusters")
public class ClusterRestController {

  private final ClusterService clusterService;

  public ClusterRestController(ClusterService clusterService) {
    this.clusterService = clusterService;
  }

  @GetMapping
  public Flux<Cluster> index() {
    return clusterService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return clusterService.count();
  }

  @GetMapping("/{id}")
  public Mono<Cluster> findById(@PathVariable Long id) {
    return clusterService.findById(id);
  }

  @PostMapping
  public Mono<Cluster> save(@RequestBody Cluster cluster) {
    return clusterService.insert(cluster);
  }

  @PutMapping("/{id}")
  public Mono<Cluster> update(@PathVariable Long id, @RequestBody Cluster cluster) {
    cluster.setId(id);
    return clusterService.update(cluster);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return clusterService.deleteById(id);
  }
}
