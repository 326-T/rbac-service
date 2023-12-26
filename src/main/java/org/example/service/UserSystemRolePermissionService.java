package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.persistence.repository.UserSystemRolePermissionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserSystemRolePermissionService {

  private final UserSystemRolePermissionRepository userSystemRolePermissionRepository;

  public UserSystemRolePermissionService(UserSystemRolePermissionRepository userSystemRolePermissionRepository) {
    this.userSystemRolePermissionRepository = userSystemRolePermissionRepository;
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param userSystemRolePermission 保存するUserSystemRolePermission
   *
   * @return 保存されたUserSystemRolePermission
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<UserSystemRolePermission> insert(UserSystemRolePermission userSystemRolePermission) {
    userSystemRolePermission.setCreatedAt(LocalDateTime.now());
    userSystemRolePermission.setUpdatedAt(LocalDateTime.now());
    return userSystemRolePermissionRepository.findDuplicate(
            userSystemRolePermission.getUserId(),
            userSystemRolePermission.getSystemRoleId())
        .flatMap(present -> Mono.<UserSystemRolePermission>error(new RedundantException("UserSystemRolePermission already exists")))
        .switchIfEmpty(Mono.just(userSystemRolePermission))
        .flatMap(userSystemRolePermissionRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long userId, Long systemRoleId) {
    return userSystemRolePermissionRepository.deleteByUniqueKeys(userId, systemRoleId);
  }
}
