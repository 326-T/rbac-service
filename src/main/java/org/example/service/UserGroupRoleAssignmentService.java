package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.persistence.repository.UserGroupRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserGroupRoleAssignmentService {

  private final UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;

  public UserGroupRoleAssignmentService(
      UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository) {
    this.userGroupRoleAssignmentRepository = userGroupRoleAssignmentRepository;
  }

  public Mono<Long> count() {
    return userGroupRoleAssignmentRepository.count();
  }

  public Flux<UserGroupRoleAssignment> findAll() {
    return userGroupRoleAssignmentRepository.findAll();
  }

  public Mono<UserGroupRoleAssignment> findById(Long id) {
    return userGroupRoleAssignmentRepository.findById(id);
  }

  public Mono<UserGroupRoleAssignment> insert(UserGroupRoleAssignment userGroupRoleAssignment) {
    userGroupRoleAssignment.setCreatedAt(LocalDateTime.now());
    userGroupRoleAssignment.setUpdatedAt(LocalDateTime.now());
    return userGroupRoleAssignmentRepository.findDuplicated(
            userGroupRoleAssignment.getNamespaceId(),
            userGroupRoleAssignment.getUserGroupId(), userGroupRoleAssignment.getRoleId())
        .flatMap(present -> Mono.<UserGroupRoleAssignment>error(new RedundantException("UserGroupRoleAssignment already exists")))
        .switchIfEmpty(Mono.just(userGroupRoleAssignment))
        .flatMap(userGroupRoleAssignmentRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return userGroupRoleAssignmentRepository.deleteById(id);
  }
}
