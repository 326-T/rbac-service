package org.example.persistence.repository;

import org.example.persistence.entity.TargetGroup;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TargetGroupRepository extends ReactiveCrudRepository<TargetGroup, Long> {

  Mono<Long> count();

  Flux<TargetGroup> findAll();

  Mono<TargetGroup> findById(Long id);

  Mono<TargetGroup> save(TargetGroup targetGroup);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT *
      FROM rbac_target_groups
      WHERE namespace_id = :namespaceId
        AND object_id_regex = :objectIdRegex;
      """)
  Mono<TargetGroup> findDuplicated(Long namespaceId, String objectIdRegex);
}
