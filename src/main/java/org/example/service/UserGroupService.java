package org.example.service;

import java.time.LocalDateTime;
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
    userGroup.setCreatedAt(LocalDateTime.now());
    userGroup.setUpdatedAt(LocalDateTime.now());
    return groupRepository.findDuplicated(userGroup.getNamespaceId(), userGroup.getName())
        .flatMap(present -> Mono.<UserGroup>error(new RedundantException("UserGroup already exists")))
        .switchIfEmpty(Mono.just(userGroup))
        .flatMap(groupRepository::save);
  }

  public Mono<UserGroup> update(UserGroup userGroup) {
    return groupRepository.findById(userGroup.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("UserGroup not found")))
        .flatMap(present -> {
          present.setName(userGroup.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        })
        .flatMap(groupRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return groupRepository.deleteById(id);
  }
}
