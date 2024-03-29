package org.example.persistence.repository;

import org.example.persistence.entity.UserGroup;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserGroupRepository extends ReactiveCrudRepository<UserGroup, Long> {

  Mono<UserGroup> findById(Long id);

  Flux<UserGroup> findByNamespaceId(Long namespaceId);

  Mono<UserGroup> save(UserGroup userGroup);

  Mono<Void> deleteById(Long id);

  @Query("SELECT * "
      + "FROM rbac_user_groups "
      + "WHERE namespace_id = :namespaceId "
      + "AND name = :name;")
  Mono<UserGroup> findDuplicate(Long namespaceId, String name);
}
