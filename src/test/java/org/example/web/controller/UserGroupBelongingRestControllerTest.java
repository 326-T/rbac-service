package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.service.ReactiveContextService;
import org.example.service.UserGroupBelongingService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.UserGroupBelongingInsertRequest;
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
    controllers = UserGroupBelongingRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class UserGroupBelongingRestControllerTest {

  @MockBean
  private UserGroupBelongingService userGroupBelongingService;
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
      @DisplayName("ユーザとグループの関係を登録できる")
      void canSaveTheUserGroupBelonging() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .id(4L).namespaceId(1L).userId(1L).userGroupId(3L).createdBy(1L).build();
        when(userGroupBelongingService.insert(any(UserGroupBelonging.class))).thenReturn(Mono.just(userGroupBelonging));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/user-group-belongings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
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
      void validationErrorOccurs(Long userId, Long userGroupId) {
        // given
        UserGroupBelongingInsertRequest userGroupBelongingInsertRequest = new UserGroupBelongingInsertRequest();
        userGroupBelongingInsertRequest.setUserId(userId);
        userGroupBelongingInsertRequest.setUserGroupId(userGroupId);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/user-group-belongings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userGroupBelongingInsertRequest)
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
      @DisplayName("ユーザとグループの関係を名前空間IDとユーザIDとユーザグループIDで削除できる")
      void canDeleteTheUserGroupBelongingByUniqueKeys() {
        // given
        when(userGroupBelongingService.deleteByUniqueKeys(1L, 2L, 3L))
            .thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri(uriBuilder -> uriBuilder.path("/rbac-service/v1/1/user-group-belongings")
                .queryParam("user-id", 2L)
                .queryParam("user-group-id", 3L)
                .build())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}