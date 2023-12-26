package org.example.service;

import java.time.LocalDateTime;
import org.example.persistence.entity.Namespace;
import org.example.persistence.entity.SystemRole;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.persistence.repository.SystemRoleRepository;
import org.example.persistence.repository.UserSystemRolePermissionRepository;
import org.example.util.constant.SystemRolePermission;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SystemRoleService {

  private final SystemRoleRepository systemRoleRepository;
  private final UserSystemRolePermissionRepository userSystemRolePermissionRepository;

  public SystemRoleService(SystemRoleRepository systemRoleRepository, UserSystemRolePermissionRepository userSystemRolePermissionRepository) {
    this.systemRoleRepository = systemRoleRepository;
    this.userSystemRolePermissionRepository = userSystemRolePermissionRepository;
  }

  public Flux<SystemRole> findByNamespaceId(Long namespaceId) {
    return systemRoleRepository.findByNamespaceId(namespaceId);
  }

  /**
   * 1. 参照権限を作成する
   * 2. 編集権限を作成する
   * 3. 特権管理者と作成者に更新権限を付与する
   *
   * @param namespace 新規作成されたNamespace
   * @param userId    Namespaceを作成したユーザーのID
   *
   * @return Void
   */
  public Mono<Void> createSystemRole(Namespace namespace, Long userId) {
    return Mono.just(SystemRole.builder()
            .namespaceId(namespace.getId())
            .name("%s_%s".formatted(namespace.getName(), SystemRolePermission.READ.getName()))
            .permission(SystemRolePermission.READ.getPermission())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build())
        .flatMap(systemRoleRepository::save)
        .then(Mono.just(SystemRole.builder()
                .namespaceId(namespace.getId())
                .name("%s_%s".formatted(namespace.getName(), SystemRolePermission.WRITE.getName()))
                .permission(SystemRolePermission.WRITE.getPermission())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build())
            .flatMap(systemRoleRepository::save).flatMapMany(savedSystemRole ->
                Flux.just(
                    UserSystemRolePermission.builder()
                        .systemRoleId(savedSystemRole.getId())
                        .userId(userId)
                        .createdBy(userId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                    UserSystemRolePermission.builder()
                        .systemRoleId(savedSystemRole.getId())
                        .userId(1L)
                        .createdBy(1L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()))
            .flatMap(userSystemRolePermissionRepository::save).then());
  }
}
