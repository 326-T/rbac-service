package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.TargetGroupBelonging;
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
public class TargetGroupBelongingAPITest {

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
      @DisplayName("ターゲットとグループの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings/count")
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
      @DisplayName("ターゲットとグループの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(TargetGroupBelonging.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                      TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                      TargetGroupBelonging::getCreatedBy)
                  .containsExactly(
                      tuple(1L, 1L, 1L, 1L, 1L),
                      tuple(2L, 2L, 2L, 2L, 2L),
                      tuple(3L, 3L, 3L, 3L, 3L));
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
      @DisplayName("ターゲットとグループの関係情報をIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings/1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroupBelonging.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                      TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                      TargetGroupBelonging::getCreatedBy)
                  .containsExactly(1L, 1L, 1L, 1L, 1L);
            });
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
      @DisplayName("ターゲットとグループの関係情報を新規登録できる")
      void insertTargetTargetClusterBelonging() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/target-group-belongings")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "targetId": 3,
                  "targetGroupId": 1
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroupBelonging.class)
            .consumeWith(response -> assertThat(response.getResponseBody())
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(4L, 1L, 3L, 1L, 2L));
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings/4")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroupBelonging.class)
            .consumeWith(response -> assertThat(response.getResponseBody())
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(4L, 1L, 3L, 1L, 2L));
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
            .uri("/rbac-service/v1/target-group-belongings")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "targetId": 1,
                  "targetGroupId": 1
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
                        "org.example.error.exception.RedundantException: TargetGroupBelonging already exists",
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
      @DisplayName("ターゲットとグループの関係情報をIDで削除できる")
      void deleteTargetTargetClusterBelongingById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/target-group-belongings/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
