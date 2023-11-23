package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Service;
import org.example.persistence.repository.ServiceRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@org.springframework.stereotype.Service
public class ServiceService {

  private final ServiceRepository serviceRepository;

  public ServiceService(ServiceRepository serviceRepository) {
    this.serviceRepository = serviceRepository;
  }

  public Mono<Long> count() {
    return serviceRepository.count();
  }

  public Flux<Service> findAll() {
    return serviceRepository.findAll();
  }

  public Mono<Service> findById(Long id) {
    return serviceRepository.findById(id);
  }

  public Mono<Service> insert(Service service) {
    if (Objects.nonNull(service.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return serviceRepository.save(service);
  }

  public Mono<Service> update(Service service) {
    return serviceRepository.findById(service.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Service not found"));
      }
      service.setUpdatedAt(LocalDateTime.now());
      service.setCreatedAt(present.getCreatedAt());
      return serviceRepository.save(service);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return serviceRepository.deleteById(id);
  }
}
