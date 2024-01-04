package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.persistence.repository.UserGroupRoleAssignmentRepository;
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
public class UserGroupRoleAssignmentAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;
  @Autowired
  private UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;

  private String jwt;
  private String readOnlyJwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
    readOnlyJwt = base64Service.encode(jwtService.encode(User.builder().id(4L).name("user3").email("zzz@example.org").build()));
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報を新規登録できる")
      void insertGroupGroupRoleAssignment() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/group-role-assignments")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userGroupId": 3,
                  "roleId": 2
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UserGroupRoleAssignment.class)
            .consumeWith(response -> assertThat(response.getResponseBody())
                .extracting(UserGroupRoleAssignment::getId, UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId, UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(4L, 2L, 3L, 2L, 2L));
        userGroupRoleAssignmentRepository.findById(4L)
            .as(StepVerifier::create)
            .assertNext(userGroupRoleAssignment ->
                assertThat(userGroupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId, UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getUserGroupId, UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 2L, 3L, 2L, 2L))
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
            .uri("/rbac-service/v1/1/group-role-assignments")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userGroupId": 1,
                  "roleId": 1
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
                        "org.example.error.exception.RedundantException: UserGroupRoleAssignment already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/group-role-assignments")
            .header(HttpHeaders.AUTHORIZATION, readOnlyJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userGroupId": 3,
                  "roleId": 2
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
                        "org.example.error.exception.UnauthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }

      @Test
      @DisplayName("ユーザグループが存在しない場合はエラーになる")
      void userGroupNotFoundCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/group-role-assignments")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userGroupId": 999,
                  "roleId": 2
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
                        404, null,
                        "idに該当するリソースが存在しない",
                        "org.example.error.exception.NotExistingException: UserGroup does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("ロールが存在しない場合はエラーになる")
      void roleNotFoundCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/group-role-assignments")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userGroupId": 3,
                  "roleId": 1
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
                        404, null,
                        "idに該当するリソースが存在しない",
                        "org.example.error.exception.NotExistingException: Role does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }

  @Order(3)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteByUniqueKeys {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報をIDで削除できる")
      void canDeleteGroupGroupRoleAssignmentByUniqueKeys() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/2/group-role-assignments?&user-group-id=3&role-id=3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        userGroupRoleAssignmentRepository.findById(3L)
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
            .uri("/rbac-service/v1/2/group-role-assignments?&user-group-id=3&role-id=3")
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
                        "org.example.error.exception.UnauthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }
    }
  }
}
