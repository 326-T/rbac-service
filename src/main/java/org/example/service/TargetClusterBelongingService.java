package org.example.service;

import java.util.Objects;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetClusterBelonging;
import org.example.persistence.repository.TargetClusterBelongingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TargetClusterBelongingService {

  private final TargetClusterBelongingRepository targetClusterBelongingRepository;

  public TargetClusterBelongingService(TargetClusterBelongingRepository targetClusterBelongingRepository) {
    this.targetClusterBelongingRepository = targetClusterBelongingRepository;
  }

  public Mono<Long> count() {
    return targetClusterBelongingRepository.count();
  }

  public Flux<TargetClusterBelonging> findAll() {
    return targetClusterBelongingRepository.findAll();
  }

  public Mono<TargetClusterBelonging> findById(Long id) {
    return targetClusterBelongingRepository.findById(id);
  }

  public Mono<TargetClusterBelonging> insert(TargetClusterBelonging targetClusterBelonging) {
    if (Objects.nonNull(targetClusterBelonging.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return targetClusterBelongingRepository.save(targetClusterBelonging);
  }

  public Mono<Void> deleteById(Long id) {
    return targetClusterBelongingRepository.deleteById(id);
  }
}
