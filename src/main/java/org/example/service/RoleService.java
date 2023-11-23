package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
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
    if (Objects.nonNull(role.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return roleRepository.save(role);
  }

  public Mono<Role> update(Role role) {
    return roleRepository.findById(role.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Role not found"));
      }
      role.setUpdatedAt(LocalDateTime.now());
      role.setCreatedAt(present.getCreatedAt());
      return roleRepository.save(role);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return roleRepository.deleteById(id);
  }
}
