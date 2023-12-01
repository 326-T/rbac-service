package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.service.UserGroupRoleAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(UserGroupRoleAssignmentRestController.class)
@AutoConfigureWebTestClient
class UserGroupRoleAssignmentRestControllerTest {

  @MockBean
  private UserGroupRoleAssignmentService userGroupRoleAssignmentService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループとロールの関係の件数を取得できる")
      void countTheIndexes() {
        // given
        when(userGroupRoleAssignmentService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/group-role-assignments/count")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Nested
  class findAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループとロールの関係を全件取得できる")
      void findAllTheIndexes() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment1 = UserGroupRoleAssignment.builder()
            .id(1L).namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        UserGroupRoleAssignment userGroupRoleAssignment2 = UserGroupRoleAssignment.builder()
            .id(2L).namespaceId(2L).userGroupId(2L).roleId(2L).createdBy(2L).build();
        UserGroupRoleAssignment userGroupRoleAssignment3 = UserGroupRoleAssignment.builder()
            .id(3L).namespaceId(3L).userGroupId(3L).roleId(3L).createdBy(3L).build();
        when(userGroupRoleAssignmentService.findAll()).thenReturn(
            Flux.just(userGroupRoleAssignment1, userGroupRoleAssignment2, userGroupRoleAssignment3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/group-role-assignments")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserGroupRoleAssignment.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserGroupRoleAssignment::getId, UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getUserGroupId, UserGroupRoleAssignment::getRoleId, UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, 1L, 1L, 1L),
                        tuple(2L, 2L, 2L, 2L, 2L),
                        tuple(3L, 3L, 3L, 3L, 3L)
                    )
            );
      }
    }
  }

  @Nested
  class findById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループとロールの関係をIDで取得できる")
      void canGetTheUserGroupRoleAssignmentById() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .id(1L).namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentService.findById(1L)).thenReturn(Mono.just(userGroupRoleAssignment));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/group-role-assignments/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroupRoleAssignment.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserGroupRoleAssignment::getId, UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getUserGroupId, UserGroupRoleAssignment::getRoleId, UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L));
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループとロールの関係を登録できる")
      void canSaveTheUserGroupRoleAssignment() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .id(4L).namespaceId(1L).userGroupId(1L).roleId(3L).createdBy(1L).build();
        when(userGroupRoleAssignmentService.insert(any(UserGroupRoleAssignment.class))).thenReturn(Mono.just(userGroupRoleAssignment));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/group-role-assignments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "userGroupId": 1,
                  "roleId": 3,
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroupRoleAssignment.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserGroupRoleAssignment::getId, UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getUserGroupId, UserGroupRoleAssignment::getRoleId, UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 1L, 1L, 3L, 1L));
      }
    }
  }

  @Nested
  class deleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループとロールの関係を削除できる")
      void canDeleteTheUserGroupRoleAssignmentById() {
        // given
        when(userGroupRoleAssignmentService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/group-role-assignments/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}