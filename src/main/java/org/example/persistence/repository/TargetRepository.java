package org.example.persistence.repository;

import org.example.persistence.entity.Target;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TargetRepository extends ReactiveCrudRepository<Target, Long> {

  Mono<Target> findById(Long id);

  @Query("""
      SELECT * FROM rbac_targets
      WHERE namespace_id = :namespaceId;
      """)
  Flux<Target> findByNamespaceId(Long namespaceId);

  @Query("""
      SELECT * FROM rbac_targets AS t
      INNER JOIN rbac_target_group_belongings AS tgb ON t.id = tgb.target_id
      WHERE t.namespace_id = :namespaceId
        AND tgb.target_group_id = :targetGroupId;
      """)
  Flux<Target> findByNamespaceIdAndTargetGroupId(Long namespaceId, Long targetGroupId);

  Mono<Target> save(Target target);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT *
      FROM rbac_targets
      WHERE namespace_id = :namespaceId
        AND object_id_regex = :objectIdRegex;
      """)
  Mono<Target> findDuplicate(Long namespaceId, String objectIdRegex);
}
