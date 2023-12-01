package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.RoleEndpointPermission;
import org.example.service.RoleEndpointPermissionService;
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

@WebFluxTest(RoleEndpointPermissionRestController.class)
@AutoConfigureWebTestClient
class RoleEndpointPermissionRestControllerTest {

  @MockBean
  private RoleEndpointPermissionService roleEndpointPermissionService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(roleEndpointPermissionService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/count")
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
      @DisplayName("ユーザグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        RoleEndpointPermission roleEndpointPermission1 = RoleEndpointPermission.builder()
            .id(1L).namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        RoleEndpointPermission roleEndpointPermission2 = RoleEndpointPermission.builder()
            .id(2L).namespaceId(2L).roleId(2L).endpointId(2L).createdBy(2L).build();
        RoleEndpointPermission roleEndpointPermission3 = RoleEndpointPermission.builder()
            .id(3L).namespaceId(3L).roleId(3L).endpointId(3L).createdBy(3L).build();
        when(roleEndpointPermissionService.findAll()).thenReturn(
            Flux.just(roleEndpointPermission1, roleEndpointPermission2, roleEndpointPermission3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(RoleEndpointPermission.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId, RoleEndpointPermission::getCreatedBy)
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
      @DisplayName("ユーザグループをIDで取得できる")
      void canGetTheRoleEndpointPermissionById() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .id(1L).namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionService.findById(1L)).thenReturn(Mono.just(roleEndpointPermission));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(RoleEndpointPermission.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId, RoleEndpointPermission::getCreatedBy)
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
      @DisplayName("ユーザグループを登録できる")
      void canSaveTheRoleEndpointPermission() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .id(4L).namespaceId(1L).roleId(1L).endpointId(3L).createdBy(1L).build();
        when(roleEndpointPermissionService.insert(any(RoleEndpointPermission.class))).thenReturn(Mono.just(roleEndpointPermission));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/role-endpoint-permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "roleId": 1,
                  "endpointId": 3,
                  "createdBy": 1
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
  }

  @Nested
  class deleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループを削除できる")
      void canDeleteTheRoleEndpointPermissionById() {
        // given
        when(roleEndpointPermissionService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/role-endpoint-permissions/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}