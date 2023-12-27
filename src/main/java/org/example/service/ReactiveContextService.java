package org.example.service;

import org.example.persistence.entity.User;
import org.example.util.constant.ContextKeys;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
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

  public User extractCurrentUser(ServerWebExchange exchange) {
    return exchange.getAttribute(ContextKeys.USER_KEY);
  }
}
