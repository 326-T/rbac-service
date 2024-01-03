package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.RoleEndpointPermission;
import org.example.persistence.entity.User;
import org.example.service.ReactiveContextService;
import org.example.service.RoleEndpointPermissionService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.RoleEndpointPermissionInsertRequest;
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
    controllers = RoleEndpointPermissionRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class RoleEndpointPermissionRestControllerTest {

  @MockBean
  private RoleEndpointPermissionService roleEndpointPermissionService;
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
      @DisplayName("ユーザグループを登録できる")
      void canSaveTheRoleEndpointPermission() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .id(4L).namespaceId(1L).roleId(1L).endpointId(3L).createdBy(1L).build();
        when(roleEndpointPermissionService.insert(any(RoleEndpointPermission.class))).thenReturn(Mono.just(roleEndpointPermission));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/role-endpoint-permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "roleId": 1,
                  "endpointId": 3
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(RoleEndpointPermission.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId, RoleEndpointPermission::getCreatedBy)
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
      void validationErrorOccurs(Long roleId, Long endpointId) {
        // given
        RoleEndpointPermissionInsertRequest roleEndpointPermissionInsertRequest = new RoleEndpointPermissionInsertRequest();
        roleEndpointPermissionInsertRequest.setRoleId(roleId);
        roleEndpointPermissionInsertRequest.setEndpointId(endpointId);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/role-endpoint-permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(roleEndpointPermissionInsertRequest)
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
      @DisplayName("ユーザグループを削除できる")
      void canDeleteTheRoleEndpointPermissionByUniqueKeys() {
        // given
        when(roleEndpointPermissionService.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/role-endpoint-permissions?role-id=1&endpoint-id=1")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}