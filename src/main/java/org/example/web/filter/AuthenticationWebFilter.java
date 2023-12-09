package org.example.web.filter;

import io.netty.util.internal.StringUtil;
import lombok.NonNull;
import org.example.error.exception.UnAuthorizedException;
import org.example.persistence.entity.User;
import org.example.service.Base64Service;
import org.example.service.JwtService;
import org.example.service.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Order(1)
@Component
public class AuthenticationWebFilter implements WebFilter {

  private final JwtService jwtService;
  private final UserService userService;
  private final Base64Service base64Service;

  public AuthenticationWebFilter(JwtService jwtService, UserService userService, Base64Service base64Service) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.base64Service = base64Service;
  }

  /**
   * 認証を行う
   *
   * @param exchange サーバーとのやり取り
   * @param chain    フィルターチェーン
   *
   * @return 認証されたユーザー
   */
  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (StringUtil.isNullOrEmpty(token)) {
      return Mono.error(new UnAuthorizedException("Authorization headerがありません。"));
    }
    return Mono.just(token)
        .filter(t -> t.startsWith("Basic "))
        .flatMap(this::basicChain)
        .switchIfEmpty(jwtChain(token))
        .flatMap(u -> chain.filter(exchange).contextWrite(Context.of(User.class, u)));
  }

  /**
   * JWT認証の場合
   *
   * @param token JWT認証のトークン
   *
   * @return 認証されたユーザー
   */
  private Mono<User> jwtChain(String token) {
    return Mono.just(token)
        .map(base64Service::decode)
        .map(jwtService::decode)
        .flatMap(u -> userService.findByEmail(u.getEmail()))
        .switchIfEmpty(Mono.error(new UnAuthorizedException("存在しないユーザです。")))
        .onErrorMap(e -> new UnAuthorizedException("Authorization headerが不正です。"));
  }

  /**
   * Basic認証の場合
   *
   * @param token Basic認証のトークン
   *
   * @return 認証されたユーザー
   */
  private Mono<User> basicChain(String token) {
    String[] decoded = base64Service.decode(token.substring("Basic ".length()).trim()).split(":");
    if (decoded.length != 2) {
      return Mono.error(new UnAuthorizedException("Authorization headerが不正です。"));
    }
    return userService.login(decoded[0], decoded[1]);
  }
}
