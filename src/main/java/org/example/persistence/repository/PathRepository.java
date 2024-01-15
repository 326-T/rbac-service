package org.example.persistence.repository;

import org.example.persistence.entity.Path;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PathRepository extends ReactiveCrudRepository<Path, Long> {

  Flux<Path> findByNamespaceId(Long namespaceId);

  Mono<Path> save(Path path);

  Mono<Void> deleteById(Long id);

  @Query("SELECT * "
      + "FROM rbac_paths "
      + "WHERE namespace_id = :namespaceId "
      + "AND regex = :regex;")
  Mono<Path> findDuplicate(Long namespaceId, String regex);
}
