package org.example.persistence.repository;

import org.example.persistence.entity.Target;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TargetRepository extends ReactiveCrudRepository<Target, Long> {

  Mono<Long> count();

  Flux<Target> findAll();

  Mono<Target> findById(Long id);

  Mono<Target> save(Target target);

  Mono<Void> deleteById(Long id);
}
