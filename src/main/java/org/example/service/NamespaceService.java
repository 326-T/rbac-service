package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
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
    if (Objects.nonNull(namespace.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return namespaceRepository.save(namespace);
  }

  public Mono<Namespace> update(Namespace namespace) {
    return namespaceRepository.findById(namespace.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Namespace not found"));
      }
      namespace.setUpdatedAt(LocalDateTime.now());
      namespace.setCreatedAt(present.getCreatedAt());
      return namespaceRepository.save(namespace);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return namespaceRepository.deleteById(id);
  }
}
