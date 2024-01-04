package org.example.web.filter;

import lombok.NonNull;
import org.example.error.exception.UnauthenticatedException;
import org.example.error.exception.UnauthorizedException;
import org.example.persistence.entity.User;
import org.example.service.SystemRoleService;
import org.example.util.PathUtil;
import org.example.util.constant.AccessPath;
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

  /**
   * 認可を行う
   * 1. OPTIONSメソッドの場合は認可を行わない
   * 2. ユーザーが存在しない場合は例外を返す
   * 3. ユーザAPIの場合は認可を行わない
   * 4. NamespaceAPIの場合は認可を行わない
   * 5. ユーザーの権限を取得する
   * 6. 権限がWRITEの場合は全てのメソッドを許可する
   * 7. 権限がWRITEでない場合はGETメソッドのみ許可する
   *
   * @param exchange the current server exchange
   * @param chain    provides a way to delegate to the next filter
   *
   * @return {@code Mono<Void>} to indicate when request handling is complete
   */
  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    HttpMethod method = exchange.getRequest().getMethod();
    if (HttpMethod.OPTIONS.equals(method)) {
      return chain.filter(exchange);
    }
    User user = exchange.getAttribute(ContextKeys.USER_KEY);
    if (user == null) {
      return Mono.error(new UnauthenticatedException("ユーザーが認証されていません。"));
    }
    String path = exchange.getRequest().getPath().value();
    if (path.startsWith(AccessPath.USERS) || path.startsWith(AccessPath.NAMESPACES) || path.startsWith(AccessPath.METHODS)) {
      return chain.filter(exchange);
    }
    return systemRoleService.aggregateSystemRolePermission(user.getId(), PathUtil.getNamespaceId(path))
        .flatMap(permissions -> {
          if (SystemRolePermission.NONE.equals(permissions)) {
            return Mono.error(new UnauthorizedException("認可されていません。"));
          }
          if (SystemRolePermission.WRITE.equals(permissions)) {
            return chain.filter(exchange);
          }
          if (HttpMethod.GET.equals(method)) {
            return chain.filter(exchange);
          }
          return Mono.error(new UnauthorizedException("認可されていません。"));
        });
  }
}
