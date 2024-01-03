package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroup;
import org.example.persistence.repository.UserGroupRepository;
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
public class UserGroupAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;
  @Autowired
  private UserGroupRepository userGroupRepository;

  private String jwt;
  private String unAuthorizedJwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
    unAuthorizedJwt = base64Service.encode(jwtService.encode(User.builder().id(4L).name("user3").email("zzz@example.org").build()));
  }

  @Order(1)
  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループを全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/user-groups")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody())
                  .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                      UserGroup::getName, UserGroup::getCreatedBy)
                  .containsExactly(
                      tuple(2L, 2L, "group2", 2L),
                      tuple(3L, 2L, "group3", 3L));
            });
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/user-groups")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
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
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループを更新できる")
      void updateUserGroup() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/user-groups/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "GROUP2"
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                      UserGroup::getName, UserGroup::getCreatedBy)
                  .containsExactly(2L, 2L, "GROUP2", 2L);
            });
        userGroupRepository.findById(2L)
            .as(StepVerifier::create)
            .assertNext(userGroup -> assertThat(userGroup)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(2L, 2L, "GROUP2", 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないユーザグループの場合はエラーになる")
      void notExistingUserGroupCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/3/user-groups/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "GROUP2"
                }
                """)
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
                        "org.example.error.exception.NotExistingException: UserGroup is not in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("namespaceIdが異なる場合はエラーになる")
      void cannotUpdateWithDifferentNamespaceId() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/3/user-groups/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "GROUP2"
                }
                """)
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
                        "org.example.error.exception.NotExistingException: UserGroup is not in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/user-groups/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "group3"
                }
                """)
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
                        "org.example.error.exception.RedundantException: UserGroup already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/user-groups/2")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "GROUP2"
                }
                """)
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
      @DisplayName("グループを新規登録できる")
      void insertUserGroup() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/user-groups")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "group4"
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroup.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                      UserGroup::getName, UserGroup::getCreatedBy)
                  .containsExactly(4L, 1L, "group4", 2L);
            });
        userGroupRepository.findById(4L)
            .as(StepVerifier::create)
            .assertNext(userGroup -> assertThat(userGroup)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(4L, 1L, "group4", 2L))
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
            .uri("/rbac-service/v1/1/user-groups")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "group1"
                }
                """)
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
                        "org.example.error.exception.RedundantException: UserGroup already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/user-groups")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "group4"
                }
                """)
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

  @Order(3)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループをIDで削除できる")
      void deleteUserGroupById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/2/user-groups/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        userGroupRepository.findById(3L)
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
            .uri("/rbac-service/v1/2/user-groups/3")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
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
      @DisplayName("存在しないターゲットの場合はエラーになる")
      void notExistingIdCauseException() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/2/user-groups/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
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
                        "org.example.error.exception.NotExistingException: UserGroup is not in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("namespaceIdが異なる場合はエラーになる")
      void cannotDeleteWithDifferentNamespaceId() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/3/user-groups/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
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
                        "org.example.error.exception.NotExistingException: UserGroup is not in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }
}
