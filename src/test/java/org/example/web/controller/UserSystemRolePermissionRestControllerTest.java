package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.service.ReactiveContextService;
import org.example.service.UserSystemRolePermissionService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.UserSystemRolePermissionInsertRequest;
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
    controllers = UserSystemRolePermissionRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class UserSystemRolePermissionRestControllerTest {

  @MockBean
  private UserSystemRolePermissionService userSystemRolePermissionService;
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
      @DisplayName("ユーザとシステムロールの関係を登録できる")
      void canSaveUserSystemRolePermission() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .id(1L).userId(2L).systemRoleId(3L).createdBy(4L).build();
        when(userSystemRolePermissionService.insert(any(UserSystemRolePermission.class), eq(1L)))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(3L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/user-system-role-permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userId": 2,
                  "systemRoleId": 3
                }
                 """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserSystemRolePermission.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getUserId,
                        UserSystemRolePermission::getSystemRoleId, UserSystemRolePermission::getCreatedBy)
                    .containsExactly(1L, 2L, 3L, 4L));
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
          "1, 0"
      })
      void validationErrorOccurs(Long userId, Long systemRoleId) {
        // given
        UserSystemRolePermissionInsertRequest request = new UserSystemRolePermissionInsertRequest();
        request.setUserId(userId);
        request.setSystemRoleId(systemRoleId);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/user-system-role-permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
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
      @DisplayName("ユーザに付与されたシステムロール権限を削除できる")
      void canDeleteUserSystemRolePermission() {
        // given
        when(userSystemRolePermissionService.deleteByUniqueKeys(3L, 9L, 1L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri(uriBuilder -> uriBuilder.path("/rbac-service/v1/1/user-system-role-permissions")
                .queryParam("user-id", 3L)
                .queryParam("system-role-id", 9L)
                .build())
            .exchange()
            .expectStatus().isNoContent();
      }
    }
  }
}