package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Role;
import org.example.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public Mono<Long> count() {
    return roleRepository.count();
  }

  public Flux<Role> findAll() {
    return roleRepository.findAll();
  }

  public Mono<Role> findById(Long id) {
    return roleRepository.findById(id);
  }

  public Mono<Role> insert(Role role) {
    role.setCreatedAt(LocalDateTime.now());
    role.setUpdatedAt(LocalDateTime.now());
    return roleRepository.findDuplicated(role.getNamespaceId(), role.getName())
        .flatMap(present -> Mono.<Role>error(new RedundantException("Role already exists")))
        .switchIfEmpty(Mono.just(role))
        .flatMap(roleRepository::save);
  }

  public Mono<Role> update(Role role) {
    return roleRepository.findById(role.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("Role not found")))
        .flatMap(present -> {
          present.setName(role.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        })
        .flatMap(roleRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return roleRepository.deleteById(id);
  }
}
