package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetGroup;
import org.example.persistence.repository.TargetGroupRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TargetGroupService {

  private final TargetGroupRepository targetGroupRepository;

  public TargetGroupService(TargetGroupRepository targetGroupRepository) {
    this.targetGroupRepository = targetGroupRepository;
  }

  public Mono<Long> count() {
    return targetGroupRepository.count();
  }

  public Flux<TargetGroup> findAll() {
    return targetGroupRepository.findAll();
  }

  public Mono<TargetGroup> findById(Long id) {
    return targetGroupRepository.findById(id);
  }

  public Mono<TargetGroup> insert(TargetGroup targetGroup) {
    targetGroup.setCreatedAt(LocalDateTime.now());
    targetGroup.setUpdatedAt(LocalDateTime.now());
    return targetGroupRepository.findDuplicated(
            targetGroup.getNamespaceId(), targetGroup.getName())
        .flatMap(present -> Mono.<TargetGroup>error(new RedundantException("TargetGroup already exists")))
        .switchIfEmpty(Mono.just(targetGroup))
        .flatMap(targetGroupRepository::save);
  }

  public Mono<TargetGroup> update(TargetGroup targetGroup) {
    return targetGroupRepository.findById(targetGroup.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("TargetGroup not found")))
        .flatMap(present -> {
          present.setName(targetGroup.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        })
        .flatMap(targetGroupRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return targetGroupRepository.deleteById(id);
  }
}
