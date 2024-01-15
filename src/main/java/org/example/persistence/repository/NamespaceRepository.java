package org.example.persistence.repository;

import org.example.persistence.entity.Namespace;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NamespaceRepository extends ReactiveCrudRepository<Namespace, Long> {

  /**
   * 参照権限以上の権限を持つNamespaceを取得する
   *
   * @param userId ユーザーID
   *
   * @return 参照権限以上の権限を持つNamespace
   */
  @Query("SELECT * FROM rbac_namespaces AS n "
      + "INNER JOIN rbac_user_system_role_permissions AS srp "
      + "ON n.id = srp.namespace_id "
      + "WHERE srp.user_id = :userId;")
  Flux<Namespace> findByUserId(Long userId);

  Mono<Namespace> save(Namespace namespace);

  Mono<Void> deleteById(Long id);

  @Query("SELECT * FROM rbac_namespaces WHERE name = :name")
  Mono<Namespace> findDuplicate(String name);
}
