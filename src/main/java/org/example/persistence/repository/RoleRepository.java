package org.example.persistence.repository;

import org.example.persistence.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {

  Mono<Long> count();

  Flux<Role> findAll();

  Mono<Role> findById(Long id);

  @Query("""
      SELECT * FROM rbac_roles
      WHERE namespace_id = :namespaceId;
      """)
  Flux<Role> findByNamespaceId(Long namespaceId);

  Mono<Role> save(Role role);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT *
      FROM rbac_roles
      WHERE namespace_id = :namespaceId
        AND name = :name;
      """)
  Mono<Role> findDuplicate(Long namespaceId, String name);
}
