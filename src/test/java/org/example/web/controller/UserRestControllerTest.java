package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.service.UserService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.UserInsertRequest;
import org.example.web.request.UserUpdateRequest;
import org.example.web.response.UserResponse;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = UserRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class UserRestControllerTest {

  @MockBean
  private UserService userService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを全件取得できる")
      void findAllTheIndexes() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq")
            .build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.")
            .build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y")
            .build();
        when(userService.findAll()).thenReturn(Flux.just(user1, user2, user3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserResponse.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(
                        tuple(1L, "user1", "xxx@example.org"),
                        tuple(2L, "user2", "yyy@example.org"),
                        tuple(3L, "user3", "zzz@example.org")
                    )
            );
      }

      @Test
      @DisplayName("ユーザをグループIDで取得できる")
      void findByUserGroupId() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("$2a$10$/MmW9CyDFA41U2nyaU7Wq.lRUjSrs0fuwP3B49WOAT2LOWQ1Tzhjq")
            .build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("$2a$10$wqoI80Es7rDralTel2nGR.W1odzTHU7RuXmKps//SUDZvSxY1Y0U.")
            .build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("$2a$10$YxMTu2M07qcQPaf4.rt2aukUFenatquwsM1WyOWbPpy9Djz7pbY.y")
            .build();
        when(userService.findByUserGroupId(1L)).thenReturn(Flux.just(user1, user2, user3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users?user-group-id=1")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserResponse.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(
                        tuple(1L, "user1", "xxx@example.org"),
                        tuple(2L, "user2", "yyy@example.org"),
                        tuple(3L, "user3", "zzz@example.org")
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
      @DisplayName("ユーザを更新できる")
      void canUpdateTheUser() {
        // given
        User user = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2")
            .build();
        when(userService.update(any(User.class))).thenReturn(Mono.just(user));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/users/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "USER2",
                  "email": "bbb@example.org",
                  "password": "PASSWORD"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(2L, "USER2", "bbb@example.org"));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", xxx@example.org, password",
          "'', xxx@example.org, password",
          "' ', xxx@example.org, password",
          "user, , password",
          "user, '', password",
          "user, ' ', password",
          "user, xxx@example.org, ",
          "user, xxx@example.org, ''",
          "user, xxx@example.org, ' '",
      })
      void validationErrorOccurs(String name, String email, String password) {
        // given
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setName(name);
        userUpdateRequest.setEmail(email);
        userUpdateRequest.setPassword(password);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userUpdateRequest)
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
      @DisplayName("ユーザを登録できる")
      void canSaveTheUser() {
        // given
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4")
            .build();
        when(userService.insert(any(User.class))).thenReturn(Mono.just(user));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "user4",
                  "email": "aaa@example.org",
                  "password": "password"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(null, "user4", "aaa@example.org"));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", xxx@example.org, password",
          "'', xxx@example.org, password",
          "' ', xxx@example.org, password",
          "user, , password",
          "user, '', password",
          "user, ' ', password",
          "user, xxx@example.org, ",
          "user, xxx@example.org, ''",
          "user, xxx@example.org, ' '",
      })
      void validationErrorOccurs(String name, String email, String password) {
        // given
        UserInsertRequest userInsertRequest = new UserInsertRequest();
        userInsertRequest.setName(name);
        userInsertRequest.setEmail(email);
        userInsertRequest.setPassword(password);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userInsertRequest)
            .exchange()
            .expectStatus().isBadRequest();
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを削除できる")
      void canDeleteTheUserById() {
        // given
        when(userService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/users/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}