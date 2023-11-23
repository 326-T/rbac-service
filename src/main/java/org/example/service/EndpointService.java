package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.repository.EndpointRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EndpointService {

  private final EndpointRepository endpointRepository;

  public EndpointService(EndpointRepository endpointRepository) {
    this.endpointRepository = endpointRepository;
  }

  public Mono<Long> count() {
    return endpointRepository.count();
  }

  public Flux<Endpoint> findAll() {
    return endpointRepository.findAll();
  }

  public Mono<Endpoint> findById(Long id) {
    return endpointRepository.findById(id);
  }

  public Mono<Endpoint> insert(Endpoint endpoint) {
    if (Objects.nonNull(endpoint.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return endpointRepository.save(endpoint);
  }

  public Mono<Endpoint> update(Endpoint endpoint) {
    return endpointRepository.findById(endpoint.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Endpoint not found"));
      }
      endpoint.setUpdatedAt(LocalDateTime.now());
      endpoint.setCreatedAt(present.getCreatedAt());
      return endpointRepository.save(endpoint);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return endpointRepository.deleteById(id);
  }
}
