package org.example.persistence.repository;

import org.example.persistence.entity.RoleEndpointPermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleEndpointPermissionRepository extends
    ReactiveCrudRepository<RoleEndpointPermission, Long> {

  Mono<RoleEndpointPermission> save(RoleEndpointPermission roleEndpointPermission);

  @Query("""
      DELETE FROM rbac_role_endpoint_permissions
      WHERE namespace_id = :namespaceId
        AND role_id = :roleId
        AND endpoint_id = :endpointId;
      """)
  Mono<Void> deleteByUniqueKeys(Long namespaceId, Long roleId, Long endpointId);

  @Query("""
      SELECT *
      FROM rbac_role_endpoint_permissions
      WHERE namespace_id = :namespaceId
        AND role_id = :roleId
        AND endpoint_id = :endpointId;
      """)
  Mono<RoleEndpointPermission> findDuplicate(Long namespaceId, Long roleId, Long endpointId);
}
