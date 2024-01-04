package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.persistence.repository.UserGroupBelongingRepository;
import org.example.persistence.repository.UserGroupRepository;
import org.example.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserGroupBelongingService {

  private final UserGroupBelongingRepository userGroupBelongingRepository;
  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;

  public UserGroupBelongingService(UserGroupBelongingRepository userGroupBelongingRepository, UserRepository userRepository,
      UserGroupRepository userGroupRepository) {
    this.userGroupBelongingRepository = userGroupBelongingRepository;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
  }

  /**
   * 1. Userが存在するか確認する
   * 2. 同じNamespaceIdのUserGroupが存在するか確認する
   * 3. 重複がないか確認する
   * 4. 保存する
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
    return userRepository.findById(userGroupBelonging.getUserId())
        .switchIfEmpty(Mono.error(new NotExistingException("User does not exist")))
        .then(userGroupRepository.findById(userGroupBelonging.getUserGroupId()))
        .filter(tg -> Objects.equals(tg.getNamespaceId(), userGroupBelonging.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("UserGroup does not exist in the namespace")))
        .then(userGroupBelongingRepository.findDuplicate(
            userGroupBelonging.getNamespaceId(),
            userGroupBelonging.getUserId(),
            userGroupBelonging.getUserGroupId()))
        .flatMap(present -> Mono.<UserGroupBelonging>error(new RedundantException("UserGroupBelonging already exists")))
        .switchIfEmpty(Mono.just(userGroupBelonging))
        .flatMap(userGroupBelongingRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long userId, Long userGroupId) {
    return userGroupBelongingRepository.deleteByUniqueKeys(namespaceId, userId, userGroupId);
  }
}
