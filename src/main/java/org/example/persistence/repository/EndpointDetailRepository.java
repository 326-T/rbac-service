package org.example.persistence.repository;

import org.example.persistence.dto.EndpointDetail;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EndpointDetailRepository extends ReactiveCrudRepository<EndpointDetail, Long> {


  @Query("""
      SELECT
        e.id,
        e.namespace_id,
        e.path_id, p.regex AS path_regex,
        e.target_group_id, tg.name AS target_group_name,
        e.method,
        e.created_by, e.created_at, e.updated_at
      FROM rbac_endpoints AS e
      INNER JOIN rbac_paths AS p ON e.path_id = p.id
      INNER JOIN rbac_target_groups AS tg ON tg.id = e.target_group_id
      WHERE e.namespace_id = :namespaceId;
      """)
  Flux<EndpointDetail> findByNamespaceId(Long namespaceId);

  @Query("""
      SELECT
        e.id,
        e.namespace_id,
        e.path_id, p.regex AS path_regex,
        e.target_group_id, tg.name AS target_group_name,
        e.method,
        e.created_by, e.created_at, e.updated_at
      FROM rbac_endpoints AS e
      INNER JOIN rbac_paths AS p ON e.path_id = p.id
      INNER JOIN rbac_target_groups AS tg ON tg.id = e.target_group_id
      INNER JOIN rbac_role_endpoint_permissions AS rep ON e.id = rep.endpoint_id
      WHERE e.namespace_id = :namespaceId
        AND rep.role_id = :roleId;
      """)
  Flux<EndpointDetail> findByNamespaceIdAndRoleId(Long namespaceId, Long roleId);
}
