package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
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
    if (Objects.nonNull(targetGroup.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return targetGroupRepository.save(targetGroup);
  }

  public Mono<TargetGroup> update(TargetGroup targetGroup) {
    return targetGroupRepository.findById(targetGroup.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Cluster not found"));
      }
      targetGroup.setUpdatedAt(LocalDateTime.now());
      targetGroup.setCreatedAt(present.getCreatedAt());
      return targetGroupRepository.save(targetGroup);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return targetGroupRepository.deleteById(id);
  }
}
