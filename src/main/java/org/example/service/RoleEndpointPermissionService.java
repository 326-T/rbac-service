package org.example.service;

import java.util.Objects;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.RoleEndpointPermission;
import org.example.persistence.repository.RoleEndpointPermissionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoleEndpointPermissionService {

  private final RoleEndpointPermissionRepository roleEndpointPermissionRepository;

  public RoleEndpointPermissionService(
      RoleEndpointPermissionRepository roleEndpointPermissionRepository) {
    this.roleEndpointPermissionRepository = roleEndpointPermissionRepository;
  }

  public Mono<Long> count() {
    return roleEndpointPermissionRepository.count();
  }

  public Flux<RoleEndpointPermission> findAll() {
    return roleEndpointPermissionRepository.findAll();
  }

  public Mono<RoleEndpointPermission> findById(Long id) {
    return roleEndpointPermissionRepository.findById(id);
  }

  public Mono<RoleEndpointPermission> insert(RoleEndpointPermission roleEndpointPermission) {
    if (Objects.nonNull(roleEndpointPermission.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return roleEndpointPermissionRepository.save(roleEndpointPermission);
  }

  public Mono<Void> deleteById(Long id) {
    return roleEndpointPermissionRepository.deleteById(id);
  }
}
