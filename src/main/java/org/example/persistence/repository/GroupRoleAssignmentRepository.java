package org.example.persistence.repository;

import org.example.persistence.entity.GroupRoleAssignment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupRoleAssignmentRepository extends
    ReactiveCrudRepository<GroupRoleAssignment, Long> {

  Mono<Long> count();

  Flux<GroupRoleAssignment> findAll();

  Mono<GroupRoleAssignment> findById(Long id);

  Mono<GroupRoleAssignment> save(GroupRoleAssignment groupRoleAssignment);

  Mono<Void> deleteById(Long id);
}
