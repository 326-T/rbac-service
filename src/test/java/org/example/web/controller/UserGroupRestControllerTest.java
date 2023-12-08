package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroup;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupService;
import org.example.web.filter.AuthenticationWebFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = UserGroupRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class UserGroupRestControllerTest {

  @MockBean
  private UserGroupService userGroupService;
  @MockBean
  private ReactiveContextService reactiveContextService;
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
        when(userGroupService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/user-groups/count")
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
        UserGroup userGroup1 = UserGroup.builder()
            .id(1L).namespaceId(1L).name("group-1").createdBy(1L).build();
        UserGroup userGroup2 = UserGroup.builder()
            .id(2L).namespaceId(2L).name("group-2").createdBy(2L).build();
        UserGroup userGroup3 = UserGroup.builder()
            .id(3L).namespaceId(3L).name("group-3").createdBy(3L).build();
        when(userGroupService.findAll()).thenReturn(Flux.just(userGroup1, userGroup2, userGroup3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/user-groups")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserGroup.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, "group-1", 1L),
                        tuple(2L, 2L, "group-2", 2L),
                        tuple(3L, 3L, "group-3", 3L)
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
      void canGetTheUserGroupById() {
        // given
        UserGroup userGroup = UserGroup.builder()
            .id(1L).namespaceId(1L).name("group-1").createdBy(1L).build();
        when(userGroupService.findById(1L)).thenReturn(Mono.just(userGroup));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/user-groups/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroup.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                        UserGroup::getName, UserGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "group-1", 1L));
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザグループを更新できる")
      void canUpdateTheUserGroup() {
        // given
        UserGroup userGroup = UserGroup.builder()
            .id(2L).namespaceId(1L).name("OBJECT-ID-2").createdBy(1L).build();
        when(userGroupService.update(any(UserGroup.class))).thenReturn(Mono.just(userGroup));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/user-groups/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "OBJECT-ID-2"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroup.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId, UserGroup::getName,
                        UserGroup::getCreatedBy)
                    .containsExactly(2L, 1L, "OBJECT-ID-2", 1L));
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
      void canSaveTheUserGroup() {
        // given
        UserGroup userGroup = UserGroup.builder()
            .id(4L).namespaceId(1L).name("group-4").createdBy(1L).build();
        when(userGroupService.insert(any(UserGroup.class))).thenReturn(Mono.just(userGroup));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/user-groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "name": "group-4"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroup.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserGroup::getId, UserGroup::getNamespaceId, UserGroup::getName,
                        UserGroup::getCreatedBy)
                    .containsExactly(4L, 1L, "group-4", 1L));
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
      void canDeleteTheUserGroupById() {
        // given
        when(userGroupService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/user-groups/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}