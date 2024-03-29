package org.example.persistence.repository;

import org.example.persistence.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

  Flux<User> findAll();

  Mono<User> findById(Long id);

  @Query("SELECT * FROM rbac_users AS u "
      + "INNER JOIN rbac_user_group_belongings AS ugb ON u.id = ugb.user_id "
      + "WHERE ugb.user_group_id = :userGroupId "
      + "ORDER BY u.id;")
  Flux<User> findByUserGroupId(Long userGroupId);

  @Query("SELECT * FROM rbac_users AS u "
      + "INNER JOIN rbac_user_system_role_permissions AS srp ON u.id = srp.user_id "
      + "WHERE srp.system_role_id = :systemRoleId "
      + "ORDER BY u.id;")
  Flux<User> findBySystemRoleId(Long systemRoleId);

  Mono<User> save(User user);

  Mono<Void> deleteById(Long id);

  Mono<User> findByEmail(String email);
}
