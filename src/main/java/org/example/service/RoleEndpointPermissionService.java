package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.RoleEndpointPermission;
import org.example.persistence.repository.EndpointRepository;
import org.example.persistence.repository.RoleEndpointPermissionRepository;
import org.example.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RoleEndpointPermissionService {

  private final RoleEndpointPermissionRepository roleEndpointPermissionRepository;
  private final RoleRepository roleRepository;
  private final EndpointRepository endpointRepository;

  public RoleEndpointPermissionService(
      RoleEndpointPermissionRepository roleEndpointPermissionRepository, RoleRepository roleRepository, EndpointRepository endpointRepository) {
    this.roleEndpointPermissionRepository = roleEndpointPermissionRepository;
    this.roleRepository = roleRepository;
    this.endpointRepository = endpointRepository;
  }

  /**
   * 1. 同じNamespaceIdのRoleが存在するか確認する
   * 2. 同じNamespaceIdのEndpointが存在するか確認する
   * 3. 重複がないか確認する
   * 4. 保存する
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
    return roleRepository.findById(roleEndpointPermission.getRoleId())
        .filter(t -> Objects.equals(t.getNamespaceId(), roleEndpointPermission.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Role does not exist in the namespace")))
        .then(endpointRepository.findById(roleEndpointPermission.getEndpointId()))
        .filter(tg -> Objects.equals(tg.getNamespaceId(), roleEndpointPermission.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Endpoint does not exist in the namespace")))
        .then(roleEndpointPermissionRepository.findDuplicate(
            roleEndpointPermission.getNamespaceId(),
            roleEndpointPermission.getRoleId(), roleEndpointPermission.getEndpointId()))
        .flatMap(present -> Mono.<RoleEndpointPermission>error(new RedundantException("RoleEndpointPermission already exists")))
        .switchIfEmpty(Mono.just(roleEndpointPermission))
        .flatMap(roleEndpointPermissionRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long roleId, Long endpointId) {
    return roleEndpointPermissionRepository.deleteByUniqueKeys(namespaceId, roleId, endpointId);
  }
}
