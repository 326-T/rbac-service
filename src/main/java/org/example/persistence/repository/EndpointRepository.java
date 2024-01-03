package org.example.persistence.repository;

import org.example.persistence.entity.Endpoint;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EndpointRepository extends
    ReactiveCrudRepository<Endpoint, Long> {

  @Query("""
      SELECT * FROM rbac_endpoints
      WHERE namespace_id = :namespaceId;
      """)
  Flux<Endpoint> findByNamespaceId(Long namespaceId);

  @Query("""
      SELECT * FROM rbac_endpoints AS e
      INNER JOIN rbac_role_endpoint_permissions AS rep ON e.id = rep.endpoint_id
      WHERE e.namespace_id = :namespaceId
        AND rep.role_id = :roleId;
      """)
  Flux<Endpoint> findByNamespaceIdAndRoleId(Long namespaceId, Long roleId);

  Mono<Endpoint> save(Endpoint endpoint);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT * FROM rbac_endpoints
      WHERE namespace_id = :namespaceId
        AND path_id = :pathId
        AND target_group_id = :targetGroupId
        AND method = :method;
      """)
  Mono<Endpoint> findDuplicate(Long namespaceId, Long pathId, Long targetGroupId, String method);
}
