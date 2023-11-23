package org.example.service;

import java.util.Objects;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.GroupRoleAssignment;
import org.example.persistence.repository.GroupRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GroupRoleAssignmentService {

  private final GroupRoleAssignmentRepository groupRoleAssignmentRepository;

  public GroupRoleAssignmentService(GroupRoleAssignmentRepository groupRoleAssignmentRepository) {
    this.groupRoleAssignmentRepository = groupRoleAssignmentRepository;
  }

  public Mono<Long> count() {
    return groupRoleAssignmentRepository.count();
  }

  public Flux<GroupRoleAssignment> findAll() {
    return groupRoleAssignmentRepository.findAll();
  }

  public Mono<GroupRoleAssignment> findById(Long id) {
    return groupRoleAssignmentRepository.findById(id);
  }

  public Mono<GroupRoleAssignment> insert(GroupRoleAssignment groupRoleAssignment) {
    if (Objects.nonNull(groupRoleAssignment.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return groupRoleAssignmentRepository.save(groupRoleAssignment);
  }

  public Mono<Void> deleteById(Long id) {
    return groupRoleAssignmentRepository.deleteById(id);
  }
}
