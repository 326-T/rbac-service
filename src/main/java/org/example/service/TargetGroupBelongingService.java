package org.example.service;

import java.util.Objects;
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
    if (Objects.nonNull(targetGroupBelonging.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return targetGroupBelongingRepository.save(targetGroupBelonging);
  }

  public Mono<Void> deleteById(Long id) {
    return targetGroupBelongingRepository.deleteById(id);
  }
}
