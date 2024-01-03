package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.persistence.repository.UserGroupBelongingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserGroupBelongingService {

  private final UserGroupBelongingRepository userGroupBelongingRepository;

  public UserGroupBelongingService(UserGroupBelongingRepository userGroupBelongingRepository) {
    this.userGroupBelongingRepository = userGroupBelongingRepository;
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param userGroupBelonging 保存するUserGroupBelonging
   *
   * @return 保存されたUserGroupBelonging
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<UserGroupBelonging> insert(UserGroupBelonging userGroupBelonging) {
    userGroupBelonging.setCreatedAt(LocalDateTime.now());
    userGroupBelonging.setUpdatedAt(LocalDateTime.now());
    return userGroupBelongingRepository.findDuplicate(
            userGroupBelonging.getNamespaceId(),
            userGroupBelonging.getUserId(),
            userGroupBelonging.getUserGroupId())
        .flatMap(present -> Mono.<UserGroupBelonging>error(new RedundantException("UserGroupBelonging already exists")))
        .switchIfEmpty(Mono.just(userGroupBelonging))
        .flatMap(userGroupBelongingRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long userId, Long userGroupId) {
    return userGroupBelongingRepository.deleteByUniqueKeys(namespaceId, userId, userGroupId);
  }
}
