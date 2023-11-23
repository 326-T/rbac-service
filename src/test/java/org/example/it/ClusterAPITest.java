package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Cluster;
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
public class ClusterAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("クラスタの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/clusters/count")
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
      @DisplayName("クラスタを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/clusters")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Cluster.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                  .containsExactly(
                      tuple(1L, "target-group-1", 1L),
                      tuple(2L, "target-group-2", 2L),
                      tuple(3L, "target-group-3", 3L));
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
      @DisplayName("クラスタをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/clusters/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Cluster.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                  .containsExactly(1L, "target-group-1", 1L);
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
      @DisplayName("クラスタを更新できる")
      void updateTargetGroup() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/clusters/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "TARGET-GROUP-2",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Cluster.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                  .containsExactly(2L, "TARGET-GROUP-2", 1L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/clusters/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Cluster.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                  .containsExactly(2L, "TARGET-GROUP-2", 1L);
            });
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("クラスタを新規登録できる")
      void insertTargetGroup() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/clusters")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "target-group-4",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Cluster.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                  .containsExactly(4L, "target-group-4", 1L);
            });
        webTestClient.get()
            .uri("/rbac-service/v1/clusters/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Cluster.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                  .containsExactly(4L, "target-group-4", 1L);
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
      @DisplayName("クラスタをIDで削除できる")
      void deleteTargetGroupById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/clusters/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/clusters/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
