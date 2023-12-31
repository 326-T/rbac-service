package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.User;
import org.example.persistence.repository.UserRepository;
import org.example.service.Base64Service;
import org.example.service.JwtService;
import org.example.web.response.UserResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebClient
public class UserAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;
  @Autowired
  private UserRepository userRepository;

  private String jwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
  }

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserResponse.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(4);
              assertThat(response.getResponseBody())
                  .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                  .containsExactly(
                      tuple(1L, "privilege", "privilege@example.org"),
                      tuple(2L, "user1", "xxx@example.org"),
                      tuple(3L, "user2", "yyy@example.org"),
                      tuple(4L, "user3", "zzz@example.org"));
            });
      }

      @Test
      @DisplayName("ユーザをユーザグループIDで取得できる")
      void findByUserGroupId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users?user-group-id=1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserResponse.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody())
                  .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                  .containsExactly(
                      tuple(2L, "user1", "xxx@example.org"));
            });
      }
    }
  }

  @Order(1)
  @Nested
  class FindBySystemRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザをシステムロールIDで取得できる")
      void findBySystemRoleId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/users/system?system-role-id=2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserResponse.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody())
                  .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                  .containsExactly(
                      tuple(1L, "privilege", "privilege@example.org"),
                      tuple(2L, "user1", "xxx@example.org"));
            });
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    class Regular {

      @Test
      @DisplayName("ユーザを更新できる")
      void updateUserUser() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/users/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "USER2",
                  "email": "bbb@example.org",
                  "password": "new_password"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(UserResponse::getId, UserResponse::getName, UserResponse::getEmail)
                    .containsExactly(3L, "USER2", "bbb@example.org")
            );
        userRepository.findById(3L)
            .as(StepVerifier::create)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(3L, "USER2", "bbb@example.org"))
            .verifyComplete();
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/users/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "user2",
                  "email": "xxx@example.org",
                  "password": "new_password"
                }
                """
            )
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        409, null,
                        "Unique制約に違反している",
                        "org.example.error.exception.RedundantException: User already exists",
                        "作成済みのリソースと重複しています。")
            );
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないユーザの場合はエラーになる")
      void notExistingUserCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/users/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "USER2",
                  "email": "bbb@example.org",
                  "password": "new_password"
                }
                """
            )
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        404, null,
                        "idに該当するリソースが存在しない",
                        "org.example.error.exception.NotExistingException: User not found",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザを新規登録できる")
      void insertUserUser() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/users")
            .header(HttpHeaders.AUTHORIZATION, jwt)
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
                    .containsExactly(5L, "user4", "aaa@example.org")
            );
        userRepository.findById(5L)
            .as(StepVerifier::create)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(5L, "user4", "aaa@example.org"))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateWithDuplicateEmail() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/users")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "user4",
                  "email": "xxx@example.org",
                  "password": "password"
                }
                """
            )
            .exchange()
            .expectStatus().is4xxClientError()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        409, null,
                        "Unique制約に違反している",
                        "org.example.error.exception.RedundantException: User already exists",
                        "作成済みのリソースと重複しています。")
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
    class Regular {

      @Test
      @DisplayName("ユーザをIDで削除できる")
      void deleteUserUserById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/users/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        userRepository.findById(3L)
            .as(StepVerifier::create)
            .verifyComplete();
      }
    }
  }
}
