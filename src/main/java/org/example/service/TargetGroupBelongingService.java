package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetGroupBelonging;
import org.example.persistence.repository.TargetGroupBelongingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TargetGroupBelongingService {

  private final TargetGroupBelongingRepository targetGroupBelongingRepository;

  public TargetGroupBelongingService(
      TargetGroupBelongingRepository targetGroupBelongingRepository) {
    this.targetGroupBelongingRepository = targetGroupBelongingRepository;
  }

  public Mono<Long> count() {
    return targetGroupBelongingRepository.count();
  }

  public Flux<TargetGroupBelonging> findAll() {
    return targetGroupBelongingRepository.findAll();
  }

  public Mono<TargetGroupBelonging> findById(Long id) {
    return targetGroupBelongingRepository.findById(id);
  }

  public Mono<TargetGroupBelonging> insert(TargetGroupBelonging targetGroupBelonging) {
    targetGroupBelonging.setCreatedAt(LocalDateTime.now());
    targetGroupBelonging.setUpdatedAt(LocalDateTime.now());
    return targetGroupBelongingRepository.findDuplicated(
            targetGroupBelonging.getNamespaceId(),
            targetGroupBelonging.getTargetGroupId(), targetGroupBelonging.getTargetId())
        .flatMap(present -> Mono.<TargetGroupBelonging>error(new RedundantException("TargetGroupBelonging already exists")))
        .switchIfEmpty(Mono.just(targetGroupBelonging))
        .flatMap(targetGroupBelongingRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return targetGroupBelongingRepository.deleteById(id);
  }
}
