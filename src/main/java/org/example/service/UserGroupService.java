package org.example.service;

import java.time.LocalDateTime;
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

  public Mono<Long> count() {
    return groupRepository.count();
  }

  public Flux<UserGroup> findAll() {
    return groupRepository.findAll();
  }

  public Mono<UserGroup> findById(Long id) {
    return groupRepository.findById(id);
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
   * 2. 変更内容をセットする
   * 3. 重複がないか確認する
   * 4. 保存する
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
        .switchIfEmpty(Mono.error(new NotExistingException("UserGroup not found")))
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

  public Mono<Void> deleteById(Long id) {
    return groupRepository.deleteById(id);
  }
}
