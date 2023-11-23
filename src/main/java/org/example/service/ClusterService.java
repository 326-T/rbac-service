package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Cluster;
import org.example.persistence.repository.ClusterRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClusterService {

  private final ClusterRepository clusterRepository;

  public ClusterService(ClusterRepository clusterRepository) {
    this.clusterRepository = clusterRepository;
  }

  public Mono<Long> count() {
    return clusterRepository.count();
  }

  public Flux<Cluster> findAll() {
    return clusterRepository.findAll();
  }

  public Mono<Cluster> findById(Long id) {
    return clusterRepository.findById(id);
  }

  public Mono<Cluster> insert(Cluster cluster) {
    if (Objects.nonNull(cluster.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return clusterRepository.save(cluster);
  }

  public Mono<Cluster> update(Cluster cluster) {
    return clusterRepository.findById(cluster.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Cluster not found"));
      }
      cluster.setUpdatedAt(LocalDateTime.now());
      cluster.setCreatedAt(present.getCreatedAt());
      return clusterRepository.save(cluster);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return clusterRepository.deleteById(id);
  }
}
