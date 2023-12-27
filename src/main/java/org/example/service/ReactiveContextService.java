package org.example.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.example.error.exception.UnAuthorizedException;
import org.example.persistence.entity.SystemRole;
import org.example.persistence.entity.User;
import org.example.util.constant.ContextKeys;
import org.example.util.constant.SystemRolePermission;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveContextService {

  /**
   * 現在のユーザーをコンテキストから取得する
   *
   * @return 現在のユーザー
   */
  public Mono<User> getCurrentUser() {
    return Mono.deferContextual(Mono::just)
        .map(context -> context.get(ContextKeys.USER_KEY));
  }

  public Mono<Void> hasWritePermission(Long namespaceId) {
    return hasPermission(namespaceId, SystemRolePermission.WRITE);
  }

  public Mono<Void> hasReadPermission(Long namespaceId) {
    return hasPermission(namespaceId, SystemRolePermission.READ, SystemRolePermission.WRITE);
  }

  /**
   * 現在のユーザーが指定した権限を持っているかどうかを判定する
   *
   * @param namespaceId 権限を判定する対象のネームスペースID
   * @param permission  権限
   *
   * @return 権限を持っているかどうか
   */
  @SuppressWarnings("unchecked")
  private Mono<Void> hasPermission(Long namespaceId, SystemRolePermission... permission) {
    return Mono.deferContextual(Mono::just)
        .map(context -> context.get(ContextKeys.ROLE_KEYS))
        .flatMapIterable(l -> (List<SystemRole>) l)
        .filter(r -> Objects.equals(r.getNamespaceId(), namespaceId))
        .any(r -> Arrays.stream(permission).anyMatch(p -> Objects.equals(r.getPermission(), p.getPermission())))
        .flatMap(b -> Boolean.TRUE.equals(b) ? Mono.empty() : Mono.error(new UnAuthorizedException("権限がありません。")));
  }
}
