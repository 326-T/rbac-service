package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Group;
import org.example.persistence.repository.GroupRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GroupService {

  private final GroupRepository groupRepository;

  public GroupService(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public Mono<Long> count() {
    return groupRepository.count();
  }

  public Flux<Group> findAll() {
    return groupRepository.findAll();
  }

  public Mono<Group> findById(Long id) {
    return groupRepository.findById(id);
  }

  public Mono<Group> insert(Group group) {
    if (Objects.nonNull(group.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return groupRepository.save(group);
  }

  public Mono<Group> update(Group group) {
    return groupRepository.findById(group.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Group not found"));
      }
      group.setUpdatedAt(LocalDateTime.now());
      group.setCreatedAt(present.getCreatedAt());
      return groupRepository.save(group);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return groupRepository.deleteById(id);
  }
}
