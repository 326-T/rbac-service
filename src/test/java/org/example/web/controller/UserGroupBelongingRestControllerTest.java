package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupBelongingService;
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
    controllers = UserGroupBelongingRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class UserGroupBelongingRestControllerTest {

  @MockBean
  private UserGroupBelongingService userGroupBelongingService;
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
      @DisplayName("ユーザとグループの関係の件数を取得できる")
      void countTheIndexes() {
        // given
        when(userGroupBelongingService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/user-group-belongings/count")
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
      @DisplayName("ユーザとグループの関係を全件取得できる")
      void findAllTheIndexes() {
        // given
        UserGroupBelonging userGroupBelonging1 = UserGroupBelonging.builder()
            .id(1L).namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        UserGroupBelonging userGroupBelonging2 = UserGroupBelonging.builder()
            .id(2L).namespaceId(2L).userId(2L).userGroupId(2L).createdBy(2L).build();
        UserGroupBelonging userGroupBelonging3 = UserGroupBelonging.builder()
            .id(3L).namespaceId(3L).userId(3L).userGroupId(3L).createdBy(3L).build();
        when(userGroupBelongingService.findAll()).thenReturn(
            Flux.just(userGroupBelonging1, userGroupBelonging2, userGroupBelonging3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/user-group-belongings")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserGroupBelonging.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId, UserGroupBelonging::getCreatedBy)
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
      @DisplayName("ユーザとグループの関係をIDで取得できる")
      void canGetTheUserGroupBelongingById() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .id(1L).namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingService.findById(1L)).thenReturn(Mono.just(userGroupBelonging));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/user-group-belongings/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroupBelonging.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId, UserGroupBelonging::getCreatedBy)
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
      @DisplayName("ユーザとグループの関係を登録できる")
      void canSaveTheUserGroupBelonging() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .id(4L).namespaceId(1L).userId(1L).userGroupId(3L).createdBy(1L).build();
        when(userGroupBelongingService.insert(any(UserGroupBelonging.class))).thenReturn(Mono.just(userGroupBelonging));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/user-group-belongings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "userId": 1,
                  "userGroupId": 3
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroupBelonging.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId, UserGroupBelonging::getCreatedBy)
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
      @DisplayName("ユーザとグループの関係を削除できる")
      void canDeleteTheUserGroupBelongingById() {
        // given
        when(userGroupBelongingService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/user-group-belongings/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}