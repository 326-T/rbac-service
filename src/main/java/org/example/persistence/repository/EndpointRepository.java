package org.example.persistence.repository;

import org.example.persistence.entity.Endpoint;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EndpointRepository extends
    ReactiveCrudRepository<Endpoint, Long> {

  Mono<Long> count();

  Flux<Endpoint> findAll();

  Mono<Endpoint> findById(Long id);

  Mono<Endpoint> save(Endpoint endpoint);

  Mono<Void> deleteById(Long id);
}
