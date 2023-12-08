package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.entity.User;
import org.example.service.JwtService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebClient
public class EndpointAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;

  private String jwt;

  @BeforeAll
  void beforeAll() {
    jwt = jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build());
  }

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/count")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(
                      tuple(1L, 1L, 1L, "GET", 1L, 1L),
                      tuple(2L, 2L, 2L, "POST", 2L, 2L),
                      tuple(3L, 3L, 3L, "PUT", 3L, 3L)
                  );
            });
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L)
            );
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントを更新できる")
      void updateTargetEndpoint() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(2L, 2L, 3L, "GET", 2L, 2L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(2L, 2L, 3L, "GET", 2L, 2L);
            });
      }
    }

    @Nested
    @DisplayName("異常系")
    class irregular {

      @Test
      @DisplayName("存在しないエンドポイントの場合はエラーになる")
      void notExistingEndpointCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/endpoints/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        404, null,
                        "idに該当するリソースが存在しない",
                        "org.example.error.exception.NotExistingException: Endpoint not found",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントを新規登録できる")
      void insertTargetEndpoint() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "pathId": 1,
                  "method": "DELETE",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(4L, 1L, 1L, "DELETE", 2L, 2L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/4")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(4L, 1L, 1L, "DELETE", 2L, 2L);
            });
      }
    }

    @Nested
    @DisplayName("異常系")
    class irregular {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicate() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "pathId": 1,
                  "method": "GET",
                  "targetGroupId": 1
                }
                """
            )
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        409, null,
                        "Unique制約に違反している",
                        "org.example.error.exception.RedundantException: Endpoint already exists",
                        "作成済みのリソースと重複しています。")
            );
      }
    }
  }

  @Order(3)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントをIDで削除できる")
      void deleteTargetEndpointById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/endpoints/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
