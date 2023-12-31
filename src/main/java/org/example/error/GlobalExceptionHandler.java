package org.example.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.error.exception.UnauthenticatedException;
import org.example.error.exception.UnauthorizedException;
import org.example.error.response.ErrorResponse;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (ex instanceof UnauthenticatedException) {
      return setResponse(exchange, HttpStatus.UNAUTHORIZED,
          ErrorResponse.builder()
              .status(HttpStatus.UNAUTHORIZED.value())
              .summary("クライアント側の認証切れ")
              .detail(ex.toString())
              .message("JWTが有効ではありません。")
              .build());
    }

    if (ex instanceof UnauthorizedException) {
      return setResponse(exchange, HttpStatus.FORBIDDEN,
          ErrorResponse.builder()
              .status(HttpStatus.FORBIDDEN.value())
              .summary("エンドポイントへのアクセス権がない")
              .detail(ex.toString())
              .message("この操作は許可されていません。")
              .build());
    }

    if (ex instanceof NotExistingException) {
      return setResponse(exchange, HttpStatus.NOT_FOUND,
          ErrorResponse.builder()
              .status(HttpStatus.NOT_FOUND.value())
              .summary("idに該当するリソースが存在しない")
              .detail(ex.toString())
              .message("指定されたリソースは存在しません。")
              .build());
    }

    if (ex instanceof RedundantException) {
      return setResponse(exchange, HttpStatus.CONFLICT,
          ErrorResponse.builder()
              .status(HttpStatus.CONFLICT.value())
              .summary("Unique制約に違反している")
              .detail(ex.toString())
              .message("作成済みのリソースと重複しています。")
              .build());
    }

    log.error("""
        予期せぬエラーが発生しました。
        %s
        """.formatted(ex.getMessage()));
    return setResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .summary("ハンドリングしていない例外が発生")
            .detail(ex.toString())
            .message("予期せぬエラーが発生しました。")
            .build());
  }

  private Mono<Void> setResponse(ServerWebExchange exchange, HttpStatusCode status, Object body) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(status);
    response.getHeaders().setAccessControlAllowOrigin("*");
    response.getHeaders().setAccessControlAllowHeaders(List.of("*"));
    response.getHeaders()
        .setAccessControlAllowMethods(List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS));
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    Flux<DataBuffer> buffer = Mono.just(body)
        .flatMap(this::writeValueAsBytes)
        .map(b -> response.bufferFactory().wrap(b))
        .flux();
    return response.writeWith(buffer);
  }

  private Mono<byte[]> writeValueAsBytes(Object object) {
    try {
      return Mono.just(objectMapper.writeValueAsBytes(object));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
