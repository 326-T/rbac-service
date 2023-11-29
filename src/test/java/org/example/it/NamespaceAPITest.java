package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Namespace;
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
public class NamespaceAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ネームスペースの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-namespace/v1/namespaces/count")
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
      @DisplayName("ネームスペースを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-namespace/v1/namespaces")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(
                      tuple(1L, "front", 1L),
                      tuple(2L, "backend", 2L),
                      tuple(3L, "database", 3L));
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
      @DisplayName("ネームスペースをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-namespace/v1/namespaces/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(1L, "front", 1L);
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
      @DisplayName("ネームスペースを更新できる")
      void updateTargetNamespace() {
        // when, then
        webTestClient.put()
            .uri("/rbac-namespace/v1/namespaces/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "BACKEND",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(2L, "BACKEND", 1L);
            });
        webTestClient.get()
            .uri("/rbac-namespace/v1/namespaces/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(2L, "BACKEND", 1L);
            });
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("ネームスペースを新規登録できる")
      void insertTargetNamespace() {
        // when, then
        webTestClient.post()
            .uri("/rbac-namespace/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "auth",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(4L, "auth", 1L);
            });
        webTestClient.get()
            .uri("/rbac-namespace/v1/namespaces/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(4L, "auth", 1L);
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
      @DisplayName("ネームスペースをIDで削除できる")
      void deleteTargetNamespaceById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-namespace/v1/namespaces/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-namespace/v1/namespaces/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
