package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.persistence.entity.SystemRole;
import org.example.persistence.entity.User;
import org.example.service.Base64Service;
import org.example.service.JwtService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebClient
public class SystemRoleAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;

  private String jwt;
  private String unAuthorizedJwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
    unAuthorizedJwt = base64Service.encode(jwtService.encode(User.builder().id(4L).name("user3").email("zzz@example.org").build()));
  }

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースIDでシステムロールを取得できる")
      void findByNamespaceId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/system-roles")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(SystemRole.class)
            .hasSize(2)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        tuple(1L, "develop_参照権限", 1L, "READ"),
                        tuple(2L, "develop_編集権限", 1L, "WRITE")
                    )
            );
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/system-roles")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
            .exchange()
            .expectStatus().isForbidden()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        403, null,
                        "エンドポイントへのアクセス権がない",
                        "org.example.error.exception.UnAuthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }
    }
  }
}
