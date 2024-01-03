package org.example.persistence.repository;

import org.example.persistence.entity.SystemRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SystemRoleRepository extends ReactiveCrudRepository<SystemRole, Long> {

  @Query("""
      SELECT * FROM rbac_system_roles
      WHERE namespace_id = :namespaceId;
      """)
  Flux<SystemRole> findByNamespaceId(Long namespaceId);

  @Query("""
      SELECT * FROM rbac_system_roles AS sr
      INNER JOIN rbac_user_system_role_permissions AS srp
      ON sr.id = srp.system_role_id
      WHERE srp.user_id = :userId
      AND sr.namespace_id = :namespaceId
      ORDER BY sr.id;
      """)
  Flux<SystemRole> findByUserIdAndNamespaceId(Long userId, Long namespaceId);

  Mono<SystemRole> save(SystemRole systemRole);
}
