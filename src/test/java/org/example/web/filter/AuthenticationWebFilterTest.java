package org.example.web.filter;

import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.service.JwtService;
import org.example.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class AuthenticationWebFilterTest {

  @InjectMocks
  private AuthenticationWebFilter authenticationWebFilter;
  @Mock
  private JwtService jwtService;
  @Mock
  private UserService userService;

  @Nested
  class filter {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("認証済みの場合、ユーザ情報をコンテキストに設定する")
      void setUserInfoOnContext() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/").header("Authorization", "sample-jwt"));
        when(jwtService.decode("sample-jwt"))
            .thenReturn(User.builder().id(1L).email("aaa@example.org").name("test").build());
        when(userService.findByEmail("aaa@example.org"))
            .thenReturn(Mono.just(User.builder().id(1L).email("aaa@example.org").name("test").build()));
        WebFilterChain chain = filter -> Mono.empty();
        // when
        authenticationWebFilter.filter(exchange, chain).block();
        // then
        StepVerifier.create(Mono.empty())
            .expectAccessibleContext()
            .hasKey(User.class);
      }
    }
  }
}