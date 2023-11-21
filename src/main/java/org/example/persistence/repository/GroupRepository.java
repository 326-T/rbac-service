package org.example.persistence.repository;

import org.example.persistence.entity.Group;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupRepository extends ReactiveCrudRepository<Group, Long> {

  Mono<Long> count();

  Flux<Group> findAll();

  Mono<Group> findById(Long id);

  Mono<Group> save(Group group);

  Mono<Void> deleteById(Long id);
}
