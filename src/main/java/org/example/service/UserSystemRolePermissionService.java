package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.persistence.repository.UserSystemRolePermissionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserSystemRolePermissionService {

  private final UserSystemRolePermissionRepository userSystemRolePermissionRepository;
  private final SystemRoleService systemRoleService;

  public UserSystemRolePermissionService(UserSystemRolePermissionRepository userSystemRolePermissionRepository, SystemRoleService systemRoleService) {
    this.userSystemRolePermissionRepository = userSystemRolePermissionRepository;
    this.systemRoleService = systemRoleService;
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param userSystemRolePermission 保存するUserSystemRolePermission
   *
   * @return 保存されたUserSystemRolePermission
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<UserSystemRolePermission> insert(UserSystemRolePermission userSystemRolePermission, Long namespaceId) {
    userSystemRolePermission.setCreatedAt(LocalDateTime.now());
    userSystemRolePermission.setUpdatedAt(LocalDateTime.now());
    return systemRoleService.findById(userSystemRolePermission.getSystemRoleId())
        .filter(systemRole -> systemRole.getNamespaceId().equals(namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("SystemRole not found")))
        .then(userSystemRolePermissionRepository.findDuplicate(
            userSystemRolePermission.getUserId(),
            userSystemRolePermission.getSystemRoleId()))
        .flatMap(present -> Mono.<UserSystemRolePermission>error(new RedundantException("UserSystemRolePermission already exists")))
        .switchIfEmpty(Mono.just(userSystemRolePermission))
        .flatMap(userSystemRolePermissionRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 削除する
   *
   * @param userId       削除するUserSystemRolePermissionのユーザーID
   * @param systemRoleId 削除するUserSystemRolePermissionのシステムロールID
   * @param namespaceId  削除するUserSystemRolePermissionのNamespaceID
   *
   * @return Void
   */
  public Mono<Void> deleteByUniqueKeys(Long userId, Long systemRoleId, Long namespaceId) {
    return systemRoleService.findById(systemRoleId)
        .filter(systemRole -> systemRole.getNamespaceId().equals(namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("SystemRole not found")))
        .then(userSystemRolePermissionRepository.deleteByUniqueKeys(userId, systemRoleId));
  }
}
