package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.response.MethodResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
    controllers = MethodController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class MethodControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("メソッド一覧を取得できる")
      void CanGetTheListOfMethods() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/methods")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(MethodResponse.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(MethodResponse::getName)
                    .containsExactly(
                        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS",
                        "(POST|PUT|DELETE|PATCH)",
                        "(GET|POST|PUT|DELETE|PATCH)")
            );
      }
    }
  }
}