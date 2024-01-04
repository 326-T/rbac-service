package org.example.persistence.repository;

import org.example.persistence.entity.UserSystemRolePermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserSystemRolePermissionRepository extends ReactiveCrudRepository<UserSystemRolePermission, Long> {

  Mono<UserSystemRolePermission> save(UserSystemRolePermission userSystemRolePermission);

  @Query("""
      DELETE FROM rbac_user_system_role_permissions
      WHERE namespace_id = :namespaceId
        AND user_id = :userId
        AND system_role_id = :systemRoleId;
      """)
  Mono<Void> deleteByUniqueKeys(Long namespaceId, Long userId, Long systemRoleId);

  @Query("""
      SELECT * FROM rbac_user_system_role_permissions
      WHERE user_id = :userId
        AND system_role_id = :systemRoleId;
      """)
  Mono<UserSystemRolePermission> findDuplicate(Long userId, Long systemRoleId);
}
