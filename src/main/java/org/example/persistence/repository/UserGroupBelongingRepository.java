package org.example.persistence.repository;

import org.example.persistence.entity.UserGroupBelonging;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserGroupBelongingRepository extends
    ReactiveCrudRepository<UserGroupBelonging, Long> {

  Mono<Long> count();

  Flux<UserGroupBelonging> findAll();

  Mono<UserGroupBelonging> findById(Long id);

  @Query("""
      SELECT * FROM rbac_user_group_belongings
      WHERE namespace_id = :namespaceId;
      """)
  Flux<UserGroupBelonging> findByNamespaceId(Long namespaceId);

  Mono<UserGroupBelonging> save(UserGroupBelonging userGroupBelonging);

  Mono<Void> deleteById(Long id);

  @Query("""
      DELETE FROM rbac_user_group_belongings
      WHERE namespace_id = :namespaceId
        AND user_id = :userId
        AND user_group_id = :userGroupId;
      """)
  Mono<Void> deleteByUniqueKeys(Long namespaceId, Long userId, Long userGroupId);

  @Query("""
      SELECT * FROM rbac_user_group_belongings
      WHERE namespace_id = :namespaceId
        AND user_id = :userId
        AND user_group_id = :userGroupId;
      """)
  Mono<UserGroupBelonging> findDuplicate(Long namespaceId, Long userId, Long userGroupId);
}
