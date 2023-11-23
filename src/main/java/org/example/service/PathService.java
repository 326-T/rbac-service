package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Path;
import org.example.persistence.repository.PathRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PathService {

  private final PathRepository pathRepository;

  public PathService(PathRepository pathRepository) {
    this.pathRepository = pathRepository;
  }

  public Mono<Long> count() {
    return pathRepository.count();
  }

  public Flux<Path> findAll() {
    return pathRepository.findAll();
  }

  public Mono<Path> findById(Long id) {
    return pathRepository.findById(id);
  }

  public Mono<Path> insert(Path path) {
    if (Objects.nonNull(path.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return pathRepository.save(path);
  }

  public Mono<Path> update(Path path) {
    return pathRepository.findById(path.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Path not found"));
      }
      path.setUpdatedAt(LocalDateTime.now());
      path.setCreatedAt(present.getCreatedAt());
      return pathRepository.save(path);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return pathRepository.deleteById(id);
  }
}
