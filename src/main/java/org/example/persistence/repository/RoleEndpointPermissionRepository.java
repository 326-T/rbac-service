package org.example.persistence.repository;

import org.example.persistence.entity.RoleEndpointPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleEndpointPermissionRepository extends
    ReactiveCrudRepository<RoleEndpointPermission, Long> {

  Mono<Long> count();

  Flux<RoleEndpointPermission> findAll();

  Mono<RoleEndpointPermission> findById(Long id);

  Mono<RoleEndpointPermission> save(RoleEndpointPermission roleEndpointPermission);

  Mono<Void> deleteById(Long id);
}
