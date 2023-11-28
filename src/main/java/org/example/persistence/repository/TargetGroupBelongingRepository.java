package org.example.persistence.repository;

import org.example.persistence.entity.TargetGroupBelonging;
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

  Mono<TargetGroupBelonging> save(TargetGroupBelonging targetGroupBelonging);

  Mono<Void> deleteById(Long id);
}
