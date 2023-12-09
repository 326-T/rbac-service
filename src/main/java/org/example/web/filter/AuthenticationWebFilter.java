package org.example.web.filter;

import io.netty.util.internal.StringUtil;
import lombok.NonNull;
import org.example.error.exception.UnAuthorizedException;
import org.example.persistence.entity.User;
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

  public AuthenticationWebFilter(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (StringUtil.isNullOrEmpty(token)) {
      return Mono.error(new UnAuthorizedException("Authorization headerがありません。"));
    }
    Mono<User> userMono = Mono.just(jwtService.decode(token))
        .flatMap(u -> userService.findByEmail(u.getEmail()))
        .switchIfEmpty(Mono.error(new UnAuthorizedException("存在しないユーザです。")))
        .onErrorMap(e -> new UnAuthorizedException("Authorization headerが不正です。"));
    return userMono.flatMap(u -> chain.filter(exchange).contextWrite(Context.of(User.class, u)));
  }
}
