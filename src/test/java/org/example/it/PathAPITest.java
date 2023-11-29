package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Path;
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
public class PathAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("パスの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths/count")
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
      @DisplayName("パスを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Path.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Path::getId, Path::getNamespaceId, Path::getRegex, Path::getCreatedBy)
                  .containsExactly(

                      tuple(1L, 1L, "/user-service/v1/", 1L),
                      tuple(2L, 2L, "/billing-service/v1/", 2L),
                      tuple(3L, 3L, "/inventory-service/v2/", 3L));
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
      @DisplayName("パスをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths/1")
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
    class regular {

      @Test
      @DisplayName("パスを更新できる")
      void updateTargetPath() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/paths/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "regex": "/replace-service/v1/",
                  "createdBy": 1
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
                    .containsExactly(2L, 1L, "/replace-service/v1/", 1L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/paths/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(2L, 1L, "/replace-service/v1/", 1L)
            );
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("パスを新規登録できる")
      void insertTargetPath() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/paths")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "regex": "/next-service/v1/",
                  "createdBy": 1
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
                    .containsExactly(4L, 1L, "/next-service/v1/", 1L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/paths/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(4L, 1L, "/next-service/v1/", 1L)
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
      @DisplayName("パスをIDで削除できる")
      void deleteTargetPathById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/paths/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/paths/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
