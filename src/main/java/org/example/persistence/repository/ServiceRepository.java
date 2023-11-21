package org.example.persistence.repository;

import org.example.persistence.entity.Service;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ServiceRepository extends ReactiveCrudRepository<Service, Long> {

  Mono<Long> count();

  Flux<Service> findAll();

  Mono<Service> findById(Long id);

  Mono<Service> save(Service service);

  Mono<Void> deleteById(Long id);
}
