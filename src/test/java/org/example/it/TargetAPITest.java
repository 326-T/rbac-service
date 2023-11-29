package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Target;
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
public class TargetAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/targets/count")
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
      @DisplayName("ターゲットを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/targets")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Target.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                      Target::getCreatedBy)
                  .containsExactly(

                      tuple(1L, 1L, "object-id-1", 1L),
                      tuple(2L, 2L, "object-id-2", 2L),
                      tuple(3L, 3L, "object-id-3", 3L));
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
      @DisplayName("ターゲットをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/targets/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                      Target::getCreatedBy)
                  .containsExactly(1L, 1L, "object-id-1", 1L);
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
    class regular {

      @Test
      @DisplayName("ターゲットを更新できる")
      void updateTargetTarget() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/targets/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "objectIdRegex": "OBJECT-ID-2",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(2L, 1L, "OBJECT-ID-2", 1L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/targets/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(2L, 1L, "OBJECT-ID-2", 1L)
            );
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("ターゲットを新規登録できる")
      void insertTargetTarget() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/targets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "objectIdRegex": "object-id-4",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(4L, 1L, "object-id-4", 1L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/targets/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(4L, 1L, "object-id-4", 1L)
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
      @DisplayName("ターゲットをIDで削除できる")
      void deleteTargetTargetById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/targets/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/targets/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
