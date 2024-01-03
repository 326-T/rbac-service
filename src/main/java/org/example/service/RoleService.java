package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Role;
import org.example.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public Flux<Role> findByNamespaceId(Long namespaceId) {
    return roleRepository.findByNamespaceId(namespaceId);
  }

  public Flux<Role> findByNamespaceIdAndUserGroupId(Long namespaceId, Long userGroupId) {
    return roleRepository.findByNamespaceIdAndUserGroupId(namespaceId, userGroupId);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param role 保存するRole
   *
   * @return 保存されたRole
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<Role> insert(Role role) {
    role.setCreatedAt(LocalDateTime.now());
    role.setUpdatedAt(LocalDateTime.now());
    return roleRepository.findDuplicate(role.getNamespaceId(), role.getName())
        .flatMap(present -> Mono.<Role>error(new RedundantException("Role already exists")))
        .switchIfEmpty(Mono.just(role))
        .flatMap(roleRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 変更内容をセットする
   * 4. 重複がないか確認する
   * 5. 保存する
   *
   * @param role nameのみ変更可能
   *
   * @return 更新されたRole
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<Role> update(Role role) {
    Mono<Role> roleMono = roleRepository.findById(role.getId())
        .filter(present -> Objects.equals(present.getNamespaceId(), role.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Role is not in the namespace")))
        .flatMap(present -> {
          present.setName(role.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return roleMono
        .flatMap(r -> roleRepository.findDuplicate(r.getNamespaceId(), r.getName()))
        .flatMap(present -> Mono.<Role>error(new RedundantException("Role already exists")))
        .switchIfEmpty(roleMono)
        .flatMap(roleRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 削除する
   *
   * @param id          RoleのID
   * @param namespaceId RoleのNamespaceId
   *
   * @return Void
   */
  public Mono<Void> deleteById(Long id, Long namespaceId) {
    return roleRepository.findById(id)
        .filter(present -> Objects.equals(present.getNamespaceId(), namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("Role is not in the namespace")))
        .map(Role::getId)
        .flatMap(roleRepository::deleteById);
  }
}
