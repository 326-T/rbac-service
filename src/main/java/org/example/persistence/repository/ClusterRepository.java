package org.example.persistence.repository;

import org.example.persistence.entity.Cluster;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ClusterRepository extends ReactiveCrudRepository<Cluster, Long> {

  Mono<Long> count();

  Flux<Cluster> findAll();

  Mono<Cluster> findById(Long id);

  Mono<Cluster> save(Cluster cluster);

  Mono<Void> deleteById(Long id);
}
