package org.example.persistence.repository;

import org.example.persistence.entity.Namespace;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NamespaceRepository extends ReactiveCrudRepository<Namespace, Long> {

  Mono<Long> count();

  Flux<Namespace> findAll();

  Mono<Namespace> findById(Long id);

  Mono<Namespace> save(Namespace namespace);

  Mono<Void> deleteById(Long id);

  @Query("SELECT * FROM rbac_namespaces WHERE name = :name")
  Mono<Namespace> findDuplicated(String name);
}
