package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Endpoint;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@AutoConfigureWebClient
public class EndpointAPITest {

  @Autowired
  private WebTestClient webTestClient;

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
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントを更新できる")
      void updateTargetEndpoint() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/endpoints/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "id": 2,
                  "namespaceId": 2,
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2,
                  "createdBy": 1
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
                  .containsExactly(2L, 2L, 3L, "GET", 2L, 1L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(2L, 2L, 3L, "GET", 2L, 1L);
            });
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("エンドポイントを新規登録できる")
      void insertTargetEndpoint() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/endpoints")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "pathId": 1,
                  "method": "DELETE",
                  "targetGroupId": 2,
                  "createdBy": 3
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
                  .containsExactly(4L, 1L, 1L, "DELETE", 2L, 3L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(4L, 1L, 1L, "DELETE", 2L, 3L);
            });
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
      @DisplayName("エンドポイントをIDで削除できる")
      void deleteTargetEndpointById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/endpoints/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
