package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.GroupRoleAssignment;
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
public class GroupRoleAssignmentAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/groupRoleAssignments/count")
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
      @DisplayName("グループとロールの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/groupRoleAssignments")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(GroupRoleAssignment.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(GroupRoleAssignment::getId, GroupRoleAssignment::getGroupId,
                      GroupRoleAssignment::getRoleId, GroupRoleAssignment::getCreatedBy)
                  .containsExactly(
                      tuple(1L, 1L, 1L, 1L),
                      tuple(2L, 2L, 2L, 2L),
                      tuple(3L, 3L, 3L, 3L));
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
      @DisplayName("グループとロールの関係情報をIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/groupRoleAssignments/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(GroupRoleAssignment.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(GroupRoleAssignment::getId, GroupRoleAssignment::getGroupId,
                      GroupRoleAssignment::getRoleId, GroupRoleAssignment::getCreatedBy)
                  .containsExactly(1L, 1L, 1L, 1L);
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
      @DisplayName("グループとロールの関係情報を新規登録できる")
      void insertGroupGroupRoleAssignment() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/groupRoleAssignments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "groupId": 3,
                  "roleId": 1,
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(GroupRoleAssignment.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(GroupRoleAssignment::getId, GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getRoleId, GroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 3L, 1L, 1L)
            );
        webTestClient.get()
            .uri("/rbac-service/v1/groupRoleAssignments/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(GroupRoleAssignment.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(GroupRoleAssignment::getId, GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getRoleId, GroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 3L, 1L, 1L)
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
      @DisplayName("グループとロールの関係情報をIDで削除できる")
      void deleteGroupGroupRoleAssignmentById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/groupRoleAssignments/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/groupRoleAssignments/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
