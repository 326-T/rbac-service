package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Namespace;
import org.example.persistence.repository.NamespaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NamespaceService {

  private final NamespaceRepository namespaceRepository;

  public NamespaceService(NamespaceRepository namespaceRepository) {
    this.namespaceRepository = namespaceRepository;
  }

  public Mono<Long> count() {
    return namespaceRepository.count();
  }

  public Flux<Namespace> findAll() {
    return namespaceRepository.findAll();
  }

  public Mono<Namespace> findById(Long id) {
    return namespaceRepository.findById(id);
  }

  public Mono<Namespace> insert(Namespace namespace) {
    namespace.setCreatedAt(LocalDateTime.now());
    namespace.setUpdatedAt(LocalDateTime.now());
    return namespaceRepository.findDuplicated(namespace.getName())
        .flatMap(present -> Mono.<Namespace>error(new RedundantException("Namespace already exists")))
        .switchIfEmpty(Mono.just(namespace))
        .flatMap(namespaceRepository::save);
  }

  public Mono<Namespace> update(Namespace namespace) {
    return namespaceRepository.findById(namespace.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("Namespace not found")))
        .flatMap(present -> {
          present.setName(namespace.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return namespaceRepository.save(present);
        });
  }

  public Mono<Void> deleteById(Long id) {
    return namespaceRepository.deleteById(id);
  }
}
