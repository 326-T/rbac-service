package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.persistence.repository.RoleRepository;
import org.example.persistence.repository.UserGroupRepository;
import org.example.persistence.repository.UserGroupRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserGroupRoleAssignmentService {

  private final UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;
  private final UserGroupRepository userGroupRepository;
  private final RoleRepository roleRepository;

  public UserGroupRoleAssignmentService(
      UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository, UserGroupRepository userGroupRepository, RoleRepository roleRepository) {
    this.userGroupRoleAssignmentRepository = userGroupRoleAssignmentRepository;
    this.userGroupRepository = userGroupRepository;
    this.roleRepository = roleRepository;
  }

  /**
   * 1. 同じNamespaceIdのUserGroupが存在するか確認する
   * 2. 同じNamespaceIdのRoleが存在するか確認する
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param userGroupRoleAssignment 保存するUserGroupRoleAssignment
   *
   * @return 保存されたUserGroupRoleAssignment
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<UserGroupRoleAssignment> insert(UserGroupRoleAssignment userGroupRoleAssignment) {
    userGroupRoleAssignment.setCreatedAt(LocalDateTime.now());
    userGroupRoleAssignment.setUpdatedAt(LocalDateTime.now());
    return userGroupRepository.findById(userGroupRoleAssignment.getUserGroupId())
        .filter(ug -> Objects.equals(ug.getNamespaceId(), userGroupRoleAssignment.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("UserGroup is not in the namespace")))
        .then(roleRepository.findById(userGroupRoleAssignment.getRoleId()))
        .filter(r -> Objects.equals(r.getNamespaceId(), userGroupRoleAssignment.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Role is not in the namespace")))
        .then(userGroupRoleAssignmentRepository.findDuplicate(
            userGroupRoleAssignment.getNamespaceId(),
            userGroupRoleAssignment.getUserGroupId(), userGroupRoleAssignment.getRoleId()))
        .flatMap(present -> Mono.<UserGroupRoleAssignment>error(new RedundantException("UserGroupRoleAssignment already exists")))
        .switchIfEmpty(Mono.just(userGroupRoleAssignment))
        .flatMap(userGroupRoleAssignmentRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long userGroupId, Long roleId) {
    return userGroupRoleAssignmentRepository.deleteByUniqueKeys(namespaceId, userGroupId, roleId);
  }
}
