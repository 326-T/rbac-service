package org.example.persistence.repository;

import org.example.persistence.entity.TargetGroup;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TargetGroupRepository extends ReactiveCrudRepository<TargetGroup, Long> {

  Mono<TargetGroup> findById(Long id);

  @Query("""
      SELECT * FROM rbac_target_groups
      WHERE namespace_id = :namespaceId;
      """)
  Flux<TargetGroup> findByNamespaceId(Long namespaceId);

  Mono<TargetGroup> save(TargetGroup targetGroup);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT *
      FROM rbac_target_groups
      WHERE namespace_id = :namespaceId
        AND name = :name;
      """)
  Mono<TargetGroup> findDuplicate(Long namespaceId, String name);
}
