package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Target;
import org.example.persistence.entity.User;
import org.example.persistence.repository.TargetRepository;
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
public class TargetAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;
  @Autowired
  private TargetRepository targetRepository;

  private String jwt;
  private String unAuthorizedJwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(2L).name("user1").email("xxx@example.org").build()));
    unAuthorizedJwt = base64Service.encode(jwtService.encode(User.builder().id(4L).name("user3").email("zzz@example.org").build()));
  }

  @Order(1)
  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットをnamespaceIdで取得できる")
      void findByNamespaceId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/targets")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Target.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody())
                  .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                      Target::getCreatedBy)
                  .containsExactly(

                      tuple(2L, 2L, "object-id-2", 2L),
                      tuple(3L, 2L, "object-id-3", 3L));
            });
      }

      @Test
      @DisplayName("ターゲットをnamespaceIdとtargetGroupIdで取得できる")
      void findByNamespaceIdAndTargetGroupId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/targets?target-group-id=3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Target.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody())
                  .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex, Target::getCreatedBy)
                  .containsExactly(
                      tuple(3L, 2L, "object-id-3", 3L));
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
            .uri("/rbac-service/v1/2/targets")
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
      @DisplayName("ターゲットを更新できる")
      void updateTargetTarget() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/targets/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "OBJECT-ID-2"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(2L, 2L, "OBJECT-ID-2", 2L)
            );
        targetRepository.findById(2L)
            .as(StepVerifier::create).assertNext(target -> assertThat(target)
                .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(2L, 2L, "OBJECT-ID-2", 2L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないターゲットの場合はエラーになる")
      void notExistingTargetCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/targets/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "OBJECT-ID-2"
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
                        "org.example.error.exception.NotExistingException: Target not found",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/targets/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "object-id-3"
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
                        "org.example.error.exception.RedundantException: Target already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/targets/2")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "OBJECT-ID-2"
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
      @DisplayName("ターゲットを新規登録できる")
      void insertTargetTarget() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/3/targets")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "object-id-4"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(4L, 3L, "object-id-4", 2L)
            );
        targetRepository.findById(4L)
            .as(StepVerifier::create).assertNext(target -> assertThat(target)
                .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(4L, 3L, "object-id-4", 2L));
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
            .uri("/rbac-service/v1/1/targets")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "object-id-1"
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
                        "org.example.error.exception.RedundantException: Target already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/3/targets")
            .header(HttpHeaders.AUTHORIZATION, unAuthorizedJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "object-id-4"
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

  @Order(3)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットをIDで削除できる")
      void deleteTargetTargetById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/2/targets/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        targetRepository.findById(3L)
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
            .uri("/rbac-service/v1/2/targets/3")
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
}
