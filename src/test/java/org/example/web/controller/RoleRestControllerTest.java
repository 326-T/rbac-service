package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Role;
import org.example.persistence.entity.User;
import org.example.service.ReactiveContextService;
import org.example.service.RoleService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.RoleUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = RoleRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class RoleRestControllerTest {

  @MockBean
  private RoleService roleService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールをnamespaceIdで取得できる")
      void canFindByNamespaceId() {
        // given
        Role role1 = Role.builder()
            .id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        Role role2 = Role.builder()
            .id(2L).namespaceId(1L).name("operator").createdBy(2L).build();
        Role role3 = Role.builder()
            .id(3L).namespaceId(1L).name("security").createdBy(3L).build();
        when(roleService.findByNamespaceId(1L)).thenReturn(Flux.just(role1, role2, role3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/roles")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Role.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, "developer", 1L),
                        tuple(2L, 1L, "operator", 2L),
                        tuple(3L, 1L, "security", 3L)
                    )
            );
      }

      @Test
      @DisplayName("ロールをnamespaceIdとuserGroupIdで取得できる")
      void canFindByNamespaceIdAndUserGroupId() {
        // given
        Role role1 = Role.builder()
            .id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        Role role2 = Role.builder()
            .id(2L).namespaceId(1L).name("operator").createdBy(2L).build();
        Role role3 = Role.builder()
            .id(3L).namespaceId(1L).name("security").createdBy(3L).build();
        when(roleService.findByNamespaceIdAndUserGroupId(1L, 1L))
            .thenReturn(Flux.just(role1, role2, role3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/roles?user-group-id=1")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Role.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, "developer", 1L),
                        tuple(2L, 1L, "operator", 2L),
                        tuple(3L, 1L, "security", 3L)
                    )
            );
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールを更新できる")
      void canUpdateTheRole() {
        // given
        Role role = Role.builder()
            .id(2L).namespaceId(1L).name("admin").createdBy(1L).build();
        when(roleService.update(any(Role.class))).thenReturn(Mono.just(role));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/1/roles/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "admin",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName,
                        Role::getCreatedBy)
                    .containsExactly(2L, 1L, "admin", 1L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String name) {
        // given
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setName(name);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/1/roles/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(roleUpdateRequest)
            .exchange()
            .expectStatus().isBadRequest();
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールを登録できる")
      void canSaveTheRole() {
        // given
        Role role = Role.builder()
            .id(4L).namespaceId(1L).name("network").createdBy(1L).build();
        when(roleService.insert(any(Role.class))).thenReturn(Mono.just(role));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "network",
                  "createdBy": 1
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Role.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName,
                        Role::getCreatedBy)
                    .containsExactly(4L, 1L, "network", 1L));
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールを削除できる")
      void canDeleteTheRoleById() {
        // given
        when(roleService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/roles/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}