package org.example.web.filter;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.example.error.exception.DifferentNamespaceException;
import org.example.error.exception.NotExistingException;
import org.example.service.TargetService;
import org.example.util.constant.AccessPath;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

@Order(3)
@Component
public class CheckNamespaceWebFilter implements WebFilter {

  private final PathPatternParser parser = new PathPatternParser();
  private final TargetService targetService;

  public CheckNamespaceWebFilter(TargetService targetService) {
    this.targetService = targetService;
  }

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    HttpMethod method = exchange.getRequest().getMethod();
    if (!HttpMethod.PUT.equals(method) && !HttpMethod.DELETE.equals(method)) {
      return chain.filter(exchange);
    }
    if (Boolean.TRUE.equals(isSamePath(exchange.getRequest().getPath(), AccessPath.TARGETS + "/{id}"))) {
      PathParams pathParams = extractPathParams(exchange.getRequest().getPath(), AccessPath.TARGETS + "/{id}");
      return targetService.findById(pathParams.getId())
          .switchIfEmpty(Mono.error(new NotExistingException("Target not found")))
          .filter(target -> target.getNamespaceId().equals(pathParams.getNamespaceId()))
          .switchIfEmpty(Mono.error(new DifferentNamespaceException("namespace-id does not match")))
          .then(chain.filter(exchange));
    }
    return chain.filter(exchange);
  }

  private Boolean isSamePath(RequestPath path, String pathPattern) {
    PathContainer pathContainer = path.pathWithinApplication();
    return parser.parse(pathPattern).matches(pathContainer);
  }

  @Builder
  @Getter
  static class PathParams {

    private final Long namespaceId;
    private final Long id;
  }

  private PathParams extractPathParams(RequestPath path, String pathPattern) {
    UriTemplate uriTemplate = new UriTemplate(pathPattern);
    Map<String, String> parameters = uriTemplate.match(path.value());
    return PathParams.builder()
        .namespaceId(Long.parseLong(parameters.get("namespace-id")))
        .id(Long.parseLong(parameters.get("id")))
        .build();
  }
}
