package org.example.web.filter;

import lombok.NonNull;
import org.example.error.exception.UnAuthenticatedException;
import org.example.error.exception.UnAuthorizedException;
import org.example.persistence.entity.User;
import org.example.service.SystemRoleService;
import org.example.util.PathUtil;
import org.example.util.constant.ContextKeys;
import org.example.util.constant.SystemRolePermission;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Order(2)
@Component
public class AuthorizationWebFilter implements WebFilter {

  private final SystemRoleService systemRoleService;

  public AuthorizationWebFilter(SystemRoleService systemRoleService) {
    this.systemRoleService = systemRoleService;
  }

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    User user = exchange.getAttribute(ContextKeys.USER_KEY);
    if (user == null) {
      return Mono.error(new UnAuthenticatedException("ユーザーが認証されていません。"));
    }
    HttpMethod method = exchange.getRequest().getMethod();
    String path = exchange.getRequest().getPath().value();
    if (HttpMethod.OPTIONS.equals(method)) {
      return chain.filter(exchange);
    }
    return systemRoleService.findByUserIdAndNamespaceId(user.getId(), PathUtil.getNamespaceId(path))
        .map(systemRole -> SystemRolePermission.of(systemRole.getPermission()))
        .switchIfEmpty(Mono.error(new UnAuthorizedException("認可されていません。")))
        .collectList()
        .flatMap(permissions -> {
          if (permissions.contains(SystemRolePermission.WRITE)) {
            return chain.filter(exchange);
          }
          if (HttpMethod.GET.equals(method)) {
            return chain.filter(exchange);
          }
          return Mono.error(new UnAuthorizedException("認可されていません。"));
        });
  }
}
