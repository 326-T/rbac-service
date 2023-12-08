package org.example.service;

import java.time.LocalDateTime;
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
    target.setCreatedAt(LocalDateTime.now());
    target.setUpdatedAt(LocalDateTime.now());
    return targetRepository.findDuplicate(
            target.getNamespaceId(), target.getObjectIdRegex())
        .flatMap(present -> Mono.<Target>error(new RedundantException("Target already exists")))
        .switchIfEmpty(Mono.just(target))
        .flatMap(targetRepository::save);
  }

  public Mono<Target> update(Target target) {
    return targetRepository.findById(target.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("Target not found")))
        .flatMap(present -> {
          present.setObjectIdRegex(target.getObjectIdRegex());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        })
        .flatMap(targetRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return targetRepository.deleteById(id);
  }
}
