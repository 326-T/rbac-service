package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Role;
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
public class RoleAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ロールの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/roles/count")
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
      @DisplayName("ロールを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/roles")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Role.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Role::getId, Role::getName, Role::getCreatedBy)
                  .containsExactly(
                      tuple(1L, "developers", 1L),
                      tuple(2L, "operations", 2L),
                      tuple(3L, "security", 3L));
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
      @DisplayName("ロールをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/roles/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Role::getId, Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, "developers", 1L)
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
      @DisplayName("ロールを更新できる")
      void updateTargetRole() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/roles/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "OPERATIONS",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Role::getId, Role::getName, Role::getCreatedBy)
                  .containsExactly(2L, "OPERATIONS", 1L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/roles/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Role::getId, Role::getName, Role::getCreatedBy)
                  .containsExactly(2L, "OPERATIONS", 1L);
            });
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("ロールを新規登録できる")
      void insertTargetRole() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "guest",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Role::getId, Role::getName, Role::getCreatedBy)
                  .containsExactly(4L, "guest", 1L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/roles/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Role::getId, Role::getName, Role::getCreatedBy)
                  .containsExactly(4L, "guest", 1L);
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
      @DisplayName("ロールをIDで削除できる")
      void deleteTargetRoleById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/roles/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/roles/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
