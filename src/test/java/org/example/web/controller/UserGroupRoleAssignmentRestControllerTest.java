package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupRoleAssignmentService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.UserGroupRoleAssignmentInsertRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = UserGroupRoleAssignmentRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class UserGroupRoleAssignmentRestControllerTest {

  @MockBean
  private UserGroupRoleAssignmentService userGroupRoleAssignmentService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザグループとロールの関係を登録できる")
      void canSaveTheUserGroupRoleAssignment() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .id(4L).namespaceId(1L).userGroupId(1L).roleId(3L).createdBy(1L).build();
        when(userGroupRoleAssignmentService.insert(any(UserGroupRoleAssignment.class))).thenReturn(Mono.just(userGroupRoleAssignment));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/group-role-assignments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userGroupId": 1,
                  "roleId": 3
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

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", 1",
          "0, 1",
          "1, ",
          "1, 0",
      })
      void validationErrorOccurs(Long userGroupId, Long roleId) {
        // given
        UserGroupRoleAssignmentInsertRequest userGroupRoleAssignmentInsertRequest = new UserGroupRoleAssignmentInsertRequest();
        userGroupRoleAssignmentInsertRequest.setUserGroupId(userGroupId);
        userGroupRoleAssignmentInsertRequest.setRoleId(roleId);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/group-role-assignments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userGroupRoleAssignmentInsertRequest)
            .exchange()
            .expectStatus().isBadRequest();
      }
    }
  }

  @Nested
  class DeleteByUniqueKeys {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザグループとロールの関係をユニークキーで削除できる")
      void canDeleteTheUserGroupRoleAssignmentByUniqueKeys() {
        // given
        when(userGroupRoleAssignmentService.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/group-role-assignments?user-group-id=1&role-id=1")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}