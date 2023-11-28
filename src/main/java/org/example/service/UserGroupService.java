package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroup;
import org.example.persistence.repository.UserGroupRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserGroupService {

  private final UserGroupRepository groupRepository;

  public UserGroupService(UserGroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public Mono<Long> count() {
    return groupRepository.count();
  }

  public Flux<UserGroup> findAll() {
    return groupRepository.findAll();
  }

  public Mono<UserGroup> findById(Long id) {
    return groupRepository.findById(id);
  }

  public Mono<UserGroup> insert(UserGroup userGroup) {
    if (Objects.nonNull(userGroup.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return groupRepository.save(userGroup);
  }

  public Mono<UserGroup> update(UserGroup userGroup) {
    return groupRepository.findById(userGroup.getId()).flatMap(present -> {
      if (Objects.isNull(present)) {
        return Mono.error(new NotExistingException("Group not found"));
      }
      userGroup.setUpdatedAt(LocalDateTime.now());
      userGroup.setCreatedAt(present.getCreatedAt());
      return groupRepository.save(userGroup);
    });
  }

  public Mono<Void> deleteById(Long id) {
    return groupRepository.deleteById(id);
  }
}
