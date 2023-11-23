package org.example.service;

import java.util.Objects;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.persistence.repository.UserGroupBelongingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserGroupBelongingService {

  private final UserGroupBelongingRepository userGroupBelongingRepository;

  public UserGroupBelongingService(UserGroupBelongingRepository userGroupBelongingRepository) {
    this.userGroupBelongingRepository = userGroupBelongingRepository;
  }

  public Mono<Long> count() {
    return userGroupBelongingRepository.count();
  }

  public Flux<UserGroupBelonging> findAll() {
    return userGroupBelongingRepository.findAll();
  }

  public Mono<UserGroupBelonging> findById(Long id) {
    return userGroupBelongingRepository.findById(id);
  }

  public Mono<UserGroupBelonging> insert(UserGroupBelonging userGroupBelonging) {
    if (Objects.nonNull(userGroupBelonging.getId())) {
      return Mono.error(new RedundantException("Id field must be empty"));
    }
    return userGroupBelongingRepository.save(userGroupBelonging);
  }

  public Mono<Void> deleteById(Long id) {
    return userGroupBelongingRepository.deleteById(id);
  }
}
