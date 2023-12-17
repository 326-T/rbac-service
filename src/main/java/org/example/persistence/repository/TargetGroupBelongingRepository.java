package org.example.persistence.repository;

import org.example.persistence.entity.TargetGroupBelonging;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TargetGroupBelongingRepository extends
    ReactiveCrudRepository<TargetGroupBelonging, Long> {

  Mono<Long> count();

  Flux<TargetGroupBelonging> findAll();

  Mono<TargetGroupBelonging> findById(Long id);

  @Query("""
      SELECT * FROM rbac_target_group_belongings
      WHERE namespace_id = :namespaceId;
      """)
  Flux<TargetGroupBelonging> findByNamespaceId(Long namespaceId);

  Mono<TargetGroupBelonging> save(TargetGroupBelonging targetGroupBelonging);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT *
      FROM rbac_target_group_belongings
      WHERE namespace_id = :namespaceId
        AND target_group_id = :targetGroupId
        AND target_id = :targetId;
      """)
  Mono<TargetGroupBelonging> findDuplicate(Long namespaceId, Long targetGroupId, Long targetId);
}
