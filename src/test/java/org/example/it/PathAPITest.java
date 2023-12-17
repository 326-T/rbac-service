package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Path;
import org.example.persistence.entity.User;
import org.example.service.Base64Service;
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
public class PathAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;

  private String jwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
  }

  @Nested
  @Order(1)
  class Count {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths/count")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Order(1)
  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths?namespace-id=2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Path.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody())
                  .extracting(Path::getId, Path::getNamespaceId, Path::getRegex, Path::getCreatedBy)
                  .containsExactly(
                      tuple(2L, 2L, "/billing-service/v1/", 2L),
                      tuple(3L, 2L, "/inventory-service/v2/", 3L));
            });
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths/1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Path::getId, Path::getNamespaceId, Path::getRegex, Path::getCreatedBy)
                  .containsExactly(1L, 1L, "/user-service/v1/", 1L);
            });
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスを更新できる")
      void updateTargetPath() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/paths/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "regex": "/replace-service/v1/"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(2L, 2L, "/replace-service/v1/", 2L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/paths/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(2L, 2L, "/replace-service/v1/", 2L)
            );
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないパスの場合はエラーになる")
      void notExistingPathCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/paths/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "regex": "/replace-service/v1/"
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
                        "org.example.error.exception.NotExistingException: Path not found",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/paths/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "regex": "/inventory-service/v2/"
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
                        "org.example.error.exception.RedundantException: Path already exists",
                        "作成済みのリソースと重複しています。")
            );
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスを新規登録できる")
      void insertTargetPath() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/paths")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "regex": "/next-service/v1/"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(4L, 1L, "/next-service/v1/", 2L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/paths/4")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(4L, 1L, "/next-service/v1/", 2L)
            );
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicate() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/paths")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "regex": "/user-service/v1/"
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
                        "org.example.error.exception.RedundantException: Path already exists",
                        "作成済みのリソースと重複しています。")
            );
      }
    }
  }

  @Order(3)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスをIDで削除できる")
      void deleteTargetPathById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/paths/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/paths/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
