package org.example.persistence.repository;

import org.example.persistence.entity.UserGroupRoleAssignment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserGroupRoleAssignmentRepository extends
    ReactiveCrudRepository<UserGroupRoleAssignment, Long> {

  Mono<Long> count();

  Flux<UserGroupRoleAssignment> findAll();

  Mono<UserGroupRoleAssignment> findById(Long id);

  Mono<UserGroupRoleAssignment> save(UserGroupRoleAssignment userGroupRoleAssignment);

  Mono<Void> deleteById(Long id);

  @Query("""
      SELECT * FROM rbac_user_group_role_assignments
      WHERE namespace_id = :namespaceId
        AND user_group_id = :userGroupId
        AND role_id = :roleId;
      """)
  Mono<UserGroupRoleAssignment> findDuplicated(Long namespaceId, Long userGroupId, Long roleId);
}
