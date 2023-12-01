package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.User;
import org.example.service.UserService;
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

@WebFluxTest(UserRestController.class)
@AutoConfigureWebTestClient
class UserRestControllerTest {

  @MockBean
  private UserService userService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザの件数を取得できる")
      void countTheIndexes() {
        // given
        when(userService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users/count")
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
      @DisplayName("ユーザを全件取得できる")
      void findAllTheIndexes() {
        // given
        User user1 = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("password_digest1").token("token1")
            .build();
        User user2 = User.builder()
            .id(2L).name("user2").email("yyy@example.org")
            .passwordDigest("password_digest2").token("token2")
            .build();
        User user3 = User.builder()
            .id(3L).name("user3").email("zzz@example.org")
            .passwordDigest("password_digest3").token("token3")
            .build();
        when(userService.findAll()).thenReturn(Flux.just(user1, user2, user3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(User.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(
                        User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(
                        tuple(1L, "user1", "xxx@example.org", "password_digest1", "token1"),
                        tuple(2L, "user2", "yyy@example.org", "password_digest2", "token2"),
                        tuple(3L, "user3", "zzz@example.org", "password_digest3", "token3")
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
      @DisplayName("ユーザをIDで取得できる")
      void canGetTheUserById() {
        // given
        User user = User.builder()
            .id(1L).name("user1").email("xxx@example.org")
            .passwordDigest("password_digest1").token("token1")
            .build();
        when(userService.findById(1L)).thenReturn(Mono.just(user));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(1L, "user1", "xxx@example.org", "password_digest1", "token1"));
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザを更新できる")
      void canUpdateTheUser() {
        // given
        User user = User.builder()
            .id(2L).name("USER2").email("bbb@example.org")
            .passwordDigest("PASSWORD_DIGEST2").token("TOKEN2")
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
                  "passwordDigest": "PASSWORD_DIGEST2",
                  "token": "TOKEN2"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2", "TOKEN2"));
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザを登録できる")
      void canSaveTheUser() {
        // given
        User user = User.builder()
            .name("user4").email("aaa@example.org")
            .passwordDigest("password_digest4").token("token4")
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
                  "passwordDigest": "password_digest4",
                  "token": "token4"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(null, "user4", "aaa@example.org", "password_digest4", "token4"));
      }
    }
  }

  @Nested
  class deleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

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