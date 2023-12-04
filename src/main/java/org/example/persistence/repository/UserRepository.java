package org.example.persistence.repository;

import org.example.persistence.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

  Mono<Long> count();

  Flux<User> findAll();

  Mono<User> findById(Long id);

  Mono<User> save(User user);

  Mono<Void> deleteById(Long id);

  @Query("SELECT * FROM rbac_users WHERE email = :email")
  Mono<User> findByEmail(String email);
}
