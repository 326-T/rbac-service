package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
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

  public Flux<UserGroup> findByNamespaceId(Long namespaceId) {
    return groupRepository.findByNamespaceId(namespaceId);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param userGroup 保存するUserGroup
   *
   * @return 保存されたUserGroup
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<UserGroup> insert(UserGroup userGroup) {
    userGroup.setCreatedAt(LocalDateTime.now());
    userGroup.setUpdatedAt(LocalDateTime.now());
    return groupRepository.findDuplicate(userGroup.getNamespaceId(), userGroup.getName())
        .flatMap(present -> Mono.<UserGroup>error(new RedundantException("UserGroup already exists")))
        .switchIfEmpty(Mono.just(userGroup))
        .flatMap(groupRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 変更内容をセットする
   * 4. 重複がないか確認する
   * 5. 保存する
   *
   * @param userGroup nameのみ変更可能
   *
   * @return 更新されたUserGroup
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<UserGroup> update(UserGroup userGroup) {
    Mono<UserGroup> userGroupMono = groupRepository.findById(userGroup.getId())
        .filter(present -> Objects.equals(present.getNamespaceId(), userGroup.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("UserGroup is not in the namespace")))
        .flatMap(present -> {
          present.setName(userGroup.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return userGroupMono
        .flatMap(g -> groupRepository.findDuplicate(g.getNamespaceId(), g.getName()))
        .flatMap(present -> Mono.<UserGroup>error(new RedundantException("UserGroup already exists")))
        .switchIfEmpty(userGroupMono)
        .flatMap(groupRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 削除する
   *
   * @param id          UserGroupのID
   * @param namespaceId UserGroupのNamespaceId
   *
   * @return Void
   */
  public Mono<Void> deleteById(Long id, Long namespaceId) {
    return groupRepository.findById(id)
        .filter(present -> Objects.equals(present.getNamespaceId(), namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("UserGroup is not in the namespace")))
        .map(UserGroup::getId)
        .flatMap(groupRepository::deleteById);
  }
}
