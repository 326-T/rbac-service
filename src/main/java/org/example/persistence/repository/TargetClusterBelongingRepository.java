package org.example.persistence.repository;

import org.example.persistence.entity.TargetClusterBelonging;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TargetClusterBelongingRepository extends
    ReactiveCrudRepository<TargetClusterBelonging, Long> {

  Mono<Long> count();

  Flux<TargetClusterBelonging> findAll();

  Mono<TargetClusterBelonging> findById(Long id);

  Mono<TargetClusterBelonging> save(TargetClusterBelonging targetClusterBelonging);

  Mono<Void> deleteById(Long id);
}
