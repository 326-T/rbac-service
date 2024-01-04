package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.groups.Tuple;
import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.dto.EndpointDetail;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.entity.User;
import org.example.persistence.repository.EndpointRepository;
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
public class EndpointAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;
  @Autowired
  private EndpointRepository endpointRepository;

  private String jwt;
  private String readOnlyJwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
    readOnlyJwt = base64Service.encode(jwtService.encode(User.builder().id(4L).name("user3").email("zzz@example.org").build()));
  }

  @Order(1)
  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをnamespaceIdで取得できる")
      void canFindByNamespaceId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(EndpointDetail.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody())
                  .extracting(EndpointDetail::getId,
                      EndpointDetail::getNamespaceId,
                      EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                      EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                      EndpointDetail::getMethod,
                      EndpointDetail::getCreatedBy)
                  .containsExactly(
                      Tuple.tuple(2L, 2L, 2L, "/billing-service/v1/", 2L, "target-group-2", "POST", 2L),
                      Tuple.tuple(3L, 2L, 3L, "/inventory-service/v2/", 3L, "target-group-3", "PUT", 3L)
                  );
            });
      }

      @Test
      @DisplayName("エンドポイントをnamespaceIdとroleIdで取得できる")
      void canFindByNamespaceIdAndRoleId() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/endpoints?role-id=2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(EndpointDetail.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody())
                  .extracting(EndpointDetail::getId,
                      EndpointDetail::getNamespaceId,
                      EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                      EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                      EndpointDetail::getMethod,
                      EndpointDetail::getCreatedBy)
                  .containsExactly(
                      Tuple.tuple(2L, 2L, 2L, "/billing-service/v1/", 2L, "target-group-2", "POST", 2L)
                  );
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
            .uri("/rbac-service/v1/2/endpoints")
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

  @Order(2)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを更新できる")
      void updateTargetEndpoint() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(2L, 2L, 3L, "GET", 2L, 2L);
            });
        endpointRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 2L, 3L, "GET", 2L, 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないエンドポイントの場合はエラーになる")
      void notExistingEndpointCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/999")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
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
                        "org.example.error.exception.NotExistingException: Endpoint does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("namespaceIdが異なる場合はエラーになる")
      void cannotUpdateWithDifferentNamespaceId() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
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
                        "org.example.error.exception.NotExistingException: Endpoint does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "PUT",
                  "targetGroupId": 3
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
                        "org.example.error.exception.RedundantException: Endpoint already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, readOnlyJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
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
                        "org.example.error.exception.UnauthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }

      @Test
      @DisplayName("存在しないパスの場合はエラーになる")
      void notExistingPathCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 1,
                  "method": "GET",
                  "targetGroupId": 2
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
                        "org.example.error.exception.NotExistingException: Path does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("存在しないターゲットグループの場合はエラーになる")
      void notExistingTargetGroupCauseException() {
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 2,
                  "method": "GET",
                  "targetGroupId": 1
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
                        "org.example.error.exception.NotExistingException: TargetGroup does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを新規登録できる")
      void insertTargetEndpoint() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 2,
                  "method": "DELETE",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(4L, 2L, 2L, "DELETE", 2L, 2L);
            });
        endpointRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(4L, 2L, 2L, "DELETE", 2L, 2L))
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
            .uri("/rbac-service/v1/1/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 1,
                  "method": "GET",
                  "targetGroupId": 1
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
                        "org.example.error.exception.RedundantException: Endpoint already exists",
                        "作成済みのリソースと重複しています。")
            );
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void notAuthorizedCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/endpoints")
            .header(HttpHeaders.AUTHORIZATION, readOnlyJwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 2,
                  "method": "DELETE",
                  "targetGroupId": 2
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
                        "org.example.error.exception.UnauthorizedException: 認可されていません。",
                        "この操作は許可されていません。")
            );
      }

      @Test
      @DisplayName("存在しないパスの場合はエラーになる")
      void notExistingPathCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 2,
                  "method": "DELETE",
                  "targetGroupId": 1
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
                        "org.example.error.exception.NotExistingException: Path does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("存在しないターゲットグループの場合はエラーになる")
      void notExistingTargetGroupCauseException() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/endpoints")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 1,
                  "method": "DELETE",
                  "targetGroupId": 2
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
                        "org.example.error.exception.NotExistingException: TargetGroup does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }

  @Order(3)
  @Nested
  @TestExecutionListeners(
      listeners = {FlywayTestExecutionListener.class},
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをIDで削除できる")
      void deleteTargetEndpointById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/2/endpoints/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        endpointRepository.findById(3L).as(StepVerifier::create).verifyComplete();
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
            .uri("/rbac-service/v1/1/endpoints/3")
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

      @Test
      @DisplayName("存在しないターゲットの場合はエラーになる")
      void notExistingIdCauseException() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/3/endpoints/999")
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
                        "org.example.error.exception.NotExistingException: Endpoint does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }

      @Test
      @DisplayName("namespaceIdが異なる場合はエラーになる")
      void cannotDeleteWithDifferentNamespaceId() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/3/endpoints/3")
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
                        "org.example.error.exception.NotExistingException: Endpoint does not exist in the namespace",
                        "指定されたリソースは存在しません。")
            );
      }
    }
  }
}
