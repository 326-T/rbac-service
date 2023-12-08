package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.TargetGroup;
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
public class TargetGroupAPITest {

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
      @DisplayName("ターゲットグループの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/count")
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
      @DisplayName("ターゲットグループを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(TargetGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                      TargetGroup::getCreatedBy)
                  .containsExactly(
                      tuple(1L, 1L, "target-group-1", 1L),
                      tuple(2L, 2L, "target-group-2", 2L),
                      tuple(3L, 3L, "target-group-3", 3L));
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
      @DisplayName("ターゲットグループをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                      TargetGroup::getCreatedBy)
                  .containsExactly(1L, 1L, "target-group-1", 1L);
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
    class regular {

      @Test
      @DisplayName("ターゲットグループを更新できる")
      void updateTargetGroup() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/target-groups/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "TARGET-GROUP-2"
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                      TargetGroup::getCreatedBy)
                  .containsExactly(2L, 2L, "TARGET-GROUP-2", 2L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                      TargetGroup::getCreatedBy)
                  .containsExactly(2L, 2L, "TARGET-GROUP-2", 2L);
            });
      }
    }

    @Nested
    @DisplayName("異常系")
    class irregular {

      @Test
      @DisplayName("存在しないターゲットグループの場合はエラーになる")
      void notExistingTargetGroupCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/target-groups/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "name": "TARGET-GROUP-2",
                  "createdBy": 1
                }
                """)
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
                        "org.example.error.exception.NotExistingException: TargetGroup not found",
                        "指定されたリソースは存在しません。")
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
    class regular {

      @Test
      @DisplayName("ターゲットグループを新規登録できる")
      void insertTargetGroup() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/target-groups")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "name": "target-group-4"
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                      TargetGroup::getCreatedBy)
                  .containsExactly(4L, 1L, "target-group-4", 2L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/4")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                      TargetGroup::getCreatedBy)
                  .containsExactly(4L, 1L, "target-group-4", 2L);
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
            .uri("/rbac-service/v1/target-groups")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "name": "target-group-1"
                }
                """)
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
                        "org.example.error.exception.RedundantException: TargetGroup already exists",
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
    class regular {

      @Test
      @DisplayName("ターゲットグループをIDで削除できる")
      void deleteTargetGroupById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/target-groups/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
