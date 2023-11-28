package org.example.persistence.repository;

import org.example.persistence.entity.UserGroup;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserGroupRepository extends ReactiveCrudRepository<UserGroup, Long> {

  Mono<Long> count();

  Flux<UserGroup> findAll();

  Mono<UserGroup> findById(Long id);

  Mono<UserGroup> save(UserGroup userGroup);

  Mono<Void> deleteById(Long id);
}
