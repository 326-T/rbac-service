package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Target;
import org.example.persistence.repository.TargetRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TargetService {

  private final TargetRepository targetRepository;

  public TargetService(TargetRepository targetRepository) {
    this.targetRepository = targetRepository;
  }

  public Mono<Long> count() {
    return targetRepository.count();
  }

  public Flux<Target> findAll() {
    return targetRepository.findAll();
  }

  public Mono<Target> findById(Long id) {
    return targetRepository.findById(id);
  }

  public Mono<Target> insert(Target target) {
    if (Objects.nonNull(target.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return targetRepository.save(target);
  }

  public Mono<Target> update(Target target) {
    return targetRepository.findById(target.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Target not found"));
      }
      target.setUpdatedAt(LocalDateTime.now());
      target.setCreatedAt(present.getCreatedAt());
      return targetRepository.save(target);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return targetRepository.deleteById(id);
  }
}
