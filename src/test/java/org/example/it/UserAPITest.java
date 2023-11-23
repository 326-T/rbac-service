package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.User;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@AutoConfigureWebClient
public class UserAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザの件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users/count")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(User.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(
                      User::getId, User::getName, User::getEmail,
                      User::getPasswordDigest, User::getToken)
                  .containsExactly(
                      tuple(1L, "user1", "xxx@example.org", "password_digest1", "token1"),
                      tuple(2L, "user2", "yyy@example.org", "password_digest2", "token2"),
                      tuple(3L, "user3", "zzz@example.org", "password_digest3", "token3"));
            });
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザをIDで取得できる")
      void findUserById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(1L, "user1", "xxx@example.org", "password_digest1", "token1")
            );
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    class regular {

      @Test
      @DisplayName("ユーザを更新できる")
      void updateUserUser() {
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
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2", "TOKEN2")
            );
        webTestClient.get()
            .uri("/rbac-service/v1/users/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(2L, "USER2", "bbb@example.org", "PASSWORD_DIGEST2", "TOKEN2")
            );
      }
    }

    @Order(2)
    @Nested
    @TestExecutionListeners(listeners = {
        FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
    class Save {

      @Test
      @DisplayName("ユーザを新規登録できる")
      void insertUserUser() {
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
                    .containsExactly(4L, "user4", "aaa@example.org", "password_digest4", "token4")
            );
        webTestClient.get()
            .uri("/rbac-service/v1/users/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(User::getId, User::getName, User::getEmail,
                        User::getPasswordDigest, User::getToken)
                    .containsExactly(4L, "user4", "aaa@example.org", "password_digest4", "token4")
            );
      }
    }
  }

  @Order(3)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ユーザをIDで削除できる")
      void deleteUserUserById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/users/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/users/3")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
