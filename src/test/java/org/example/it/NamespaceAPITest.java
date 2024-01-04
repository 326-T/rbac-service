package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Namespace;
import org.example.persistence.entity.SystemRole;
import org.example.persistence.entity.User;
import org.example.persistence.repository.NamespaceRepository;
import org.example.persistence.repository.SystemRoleRepository;
import org.example.persistence.repository.UserRepository;
import org.example.service.Base64Service;
import org.example.service.JwtService;
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
public class NamespaceAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;
  @Autowired
  private NamespaceRepository namespaceRepository;
  @Autowired
  private SystemRoleRepository systemRoleRepository;
  @Autowired
  private UserRepository userRepository;

  private String jwt;
  private String readOnlyJwt;
  private String unAuthorizedJwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(2L).name("user1").email("xxx@example.org").build()));
    readOnlyJwt = base64Service.encode(jwtService.encode(User.builder().id(3L).name("user2").email("yyy@example.org").build()));
    unAuthorizedJwt = base64Service.encode(jwtService.encode(User.builder().id(4L).name("user3").email("zzz@example.org").build()));
  }

  @Order(1)
  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/namespaces")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(
                      tuple(1L, "develop", 1L),
                      tuple(2L, "staging", 2L),
                      tuple(3L, "production", 3L));
            });
      }

      @Test
      @DisplayName("権限がなければ何も表示されない")
      void findAllTheIndexesWithNoPermission() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/namespaces")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Namespace.class)
            .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを更新できる")
      void updateTargetNamespace() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/namespaces/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "STAGING"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                  .containsExactly(2L, "STAGING", 2L);
            });
        namespaceRepository.findById(2L)
            .as(StepVerifier::create)
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(2L, "STAGING", 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないネームスペースの場合はエラーになる")
      void notExistingNamespaceCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/namespaces/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                "name": "STAGING"
                }
                """
            )
            .exchange()
            .expectStatus().isForbidden()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        403, null,
                        "エンドポイントへのアクセス権がない",
                        "org.example.error.exception.UnAuthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/namespaces/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "production"
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
                        "org.example.error.exception.RedundantException: Namespace already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/namespaces/2")
            .header(HttpHeaders.AUTHORIZATION, readOnlyJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "STAGING"
                }
                """
            )
            .exchange()
            .expectStatus().isForbidden()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        403, null,
                        "エンドポイントへのアクセス権がない",
                        "org.example.error.exception.UnAuthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
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
      @DisplayName("ネームスペースを新規登録できる")
      void insertTargetNamespace() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/namespaces")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "integration"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(4L, "integration", 2L)
            );
        namespaceRepository.findById(4L)
            .as(StepVerifier::create)
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(4L, "integration", 2L))
            .verifyComplete();
        // システムロールが自動作成されている
        systemRoleRepository.findByNamespaceId(4L)
            .as(StepVerifier::create)
            .assertNext(systemRole -> assertThat(systemRole)
                .extracting(
                    SystemRole::getId, SystemRole::getName,
                    SystemRole::getNamespaceId, SystemRole::getPermission)
                .containsExactly(7L, "integration_参照権限", 4L, "READ"))
            .assertNext(systemRole -> assertThat(systemRole)
                .extracting(
                    SystemRole::getId, SystemRole::getName,
                    SystemRole::getNamespaceId, SystemRole::getPermission)
                .containsExactly(8L, "integration_編集権限", 4L, "WRITE"))
            .verifyComplete();
        // システムロールに作成者と特権管理者が紐づいている
        userRepository.findBySystemRoleId(7L)
            .as(StepVerifier::create)
            .verifyComplete();
        userRepository.findBySystemRoleId(8L)
            .as(StepVerifier::create)
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(1L, "privilege", "privilege@example.org"))
            .assertNext(user -> assertThat(user)
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(2L, "user1", "xxx@example.org"))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicate() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/namespaces")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "develop"
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
                        "org.example.error.exception.RedundantException: Namespace already exists",
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
      @DisplayName("ネームスペースをIDで削除できる")
      void deleteTargetNamespaceById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/namespaces/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        namespaceRepository.findById(3L)
            .as(StepVerifier::create)
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/namespaces/3")
            .header(HttpHeaders.AUTHORIZATION, readOnlyJwt)
            .exchange()
            .expectStatus().isForbidden()
            .expectBody(ErrorResponse.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(
                        ErrorResponse::getStatus, ErrorResponse::getCode,
                        ErrorResponse::getSummary, ErrorResponse::getDetail, ErrorResponse::getMessage)
                    .containsExactly(
                        403, null,
                        "エンドポイントへのアクセス権がない",
                        "org.example.error.exception.UnAuthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }
    }
  }
}
