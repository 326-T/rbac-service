package org.example.persistence.repository;

import org.example.persistence.dto.AccessPrivilege;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AccessPrivilegeRepository extends ReactiveCrudRepository<AccessPrivilege, Long> {


  @Query("""
      SELECT
        u.id AS user_id, u.name AS user_name,
        n.id AS namespace_id, n.name AS namespace_name,
        ug.id AS user_group_id, ug.name AS user_group_name,
        r.id AS role_id, r.name AS role_name,
        p.id AS path_id, p.regex AS path_regex, e.method AS method,
        tg.id AS target_group_id, tg.name AS target_group_name,
        t.id AS target_id, t.object_id_regex AS object_id_regex
      FROM rbac_users AS u
      INNER JOIN rbac_user_group_belongings AS ugb ON u.id = ugb.user_id
      INNER JOIN rbac_namespaces AS n ON ugb.namespace_id = n.id
      INNER JOIN rbac_user_groups AS ug ON ugb.user_group_id = ug.id
      INNER JOIN rbac_group_role_assignments AS gra ON ug.id = gra.user_group_id
      INNER JOIN rbac_roles AS r ON gra.role_id = r.id
      INNER JOIN rbac_role_endpoint_permissions AS rep ON r.id = rep.role_id
      INNER JOIN rbac_endpoints AS e ON rep.endpoint_id = e.id
      INNER JOIN rbac_paths AS p ON e.path_id = p.id
      INNER JOIN rbac_target_groups AS tg ON tg.id = e.target_group_id
      INNER JOIN rbac_target_group_belongings AS tgb ON tg.id = tgb.target_group_id
      INNER JOIN rbac_targets AS t ON tgb.target_id = t.id
      WHERE n.id = :namespaceId;
      """)
  Flux<AccessPrivilege> findByNamespace(Long namespaceId);

  @Query("""
      SELECT
        u.id AS user_id, u.name AS user_name,
        n.id AS namespace_id, n.name AS namespace_name,
        ug.id AS user_group_id, ug.name AS user_group_name,
        r.id AS role_id, r.name AS role_name,
        p.id AS path_id, p.regex AS path_regex, e.method AS method,
        tg.id AS target_group_id, tg.name AS target_group_name,
        t.id AS target_id, t.object_id_regex AS object_id_regex
      FROM rbac_users AS u
      INNER JOIN rbac_user_group_belongings AS ugb ON u.id = ugb.user_id
      INNER JOIN rbac_namespaces AS n ON ugb.namespace_id = n.id
      INNER JOIN rbac_user_groups AS ug ON ugb.user_group_id = ug.id
      INNER JOIN rbac_group_role_assignments AS gra ON ug.id = gra.user_group_id
      INNER JOIN rbac_roles AS r ON gra.role_id = r.id
      INNER JOIN rbac_role_endpoint_permissions AS rep ON r.id = rep.role_id
      INNER JOIN rbac_endpoints AS e ON rep.endpoint_id = e.id
      INNER JOIN rbac_paths AS p ON e.path_id = p.id
      INNER JOIN rbac_target_groups AS tg ON tg.id = e.target_group_id
      INNER JOIN rbac_target_group_belongings AS tgb ON tg.id = tgb.target_group_id
      INNER JOIN rbac_targets AS t ON tgb.target_id = t.id
      WHERE u.id = :userId;
      """)
  Flux<AccessPrivilege> findByUser(Long userId);
}
