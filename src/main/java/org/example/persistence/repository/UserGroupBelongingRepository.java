package org.example.persistence.repository;

import org.example.persistence.entity.UserGroupBelonging;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserGroupBelongingRepository extends
    ReactiveCrudRepository<UserGroupBelonging, Long> {

  Mono<Long> count();

  Flux<UserGroupBelonging> findAll();

  Mono<UserGroupBelonging> findById(Long id);

  Mono<UserGroupBelonging> save(UserGroupBelonging userGroupBelonging);

  Mono<Void> deleteById(Long id);
}
