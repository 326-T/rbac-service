package org.example.web.controller;

import org.example.persistence.entity.Service;
import org.example.service.ServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rbac-service/v1/services")
public class ServiceController {

  private final ServiceService serviceService;

  public ServiceController(ServiceService serviceService) {
    this.serviceService = serviceService;
  }

  @GetMapping
  public Flux<Service> index() {
    return serviceService.findAll();
  }

  @GetMapping("/count")
  public Mono<Long> count() {
    return serviceService.count();
  }

  @GetMapping("/{id}")
  public Mono<Service> findById(@PathVariable Long id) {
    return serviceService.findById(id);
  }

  @PostMapping
  public Mono<Service> save(@RequestBody Service service) {
    return serviceService.insert(service);
  }

  @PutMapping("/{id}")
  public Mono<Service> update(@PathVariable Long id, @RequestBody Service service) {
    service.setId(id);
    return serviceService.update(service);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return serviceService.deleteById(id);
  }
}
