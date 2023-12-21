package org.example.service;

import java.time.LocalDateTime;
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

  public Flux<RoleEndpointPermission> findByNamespaceId(Long namespaceId) {
    return roleEndpointPermissionRepository.findByNamespaceId(namespaceId);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param roleEndpointPermission 保存するRoleEndpointPermission
   *
   * @return 保存されたRoleEndpointPermission
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<RoleEndpointPermission> insert(RoleEndpointPermission roleEndpointPermission) {
    roleEndpointPermission.setCreatedAt(LocalDateTime.now());
    roleEndpointPermission.setUpdatedAt(LocalDateTime.now());
    return roleEndpointPermissionRepository.findDuplicate(
            roleEndpointPermission.getNamespaceId(),
            roleEndpointPermission.getRoleId(), roleEndpointPermission.getEndpointId())
        .flatMap(present -> Mono.<RoleEndpointPermission>error(new RedundantException("RoleEndpointPermission already exists")))
        .switchIfEmpty(Mono.just(roleEndpointPermission))
        .flatMap(roleEndpointPermissionRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return roleEndpointPermissionRepository.deleteById(id);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long roleId, Long endpointId) {
    return roleEndpointPermissionRepository.deleteByUniqueKeys(namespaceId, roleId, endpointId);
  }
}
