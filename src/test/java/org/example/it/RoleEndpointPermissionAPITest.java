package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import org.example.Application;
import org.example.error.response.ErrorResponse;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.RoleEndpointPermission;
import org.example.persistence.entity.User;
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

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebClient
public class RoleEndpointPermissionAPITest {

  @Autowired
  private WebTestClient webTestClient;
  @Autowired
  private JwtService jwtService;
  @Autowired
  private Base64Service base64Service;

  private String jwt;

  @BeforeAll
  void beforeAll() {
    jwt = base64Service.encode(jwtService.encode(User.builder().id(1L).name("user1").email("xxx@example.org").build()));
  }

  @Nested
  @Order(1)
  class Count {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールとエンドポイントの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/count")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Order(1)
  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールとエンドポイントの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions?namespace-id=2")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(RoleEndpointPermission.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody()).hasSize(2);
              assertThat(response.getResponseBody())
                  .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                      RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId,
                      RoleEndpointPermission::getCreatedBy)
                  .containsExactly(
                      tuple(2L, 2L, 2L, 2L, 2L),
                      tuple(3L, 2L, 3L, 3L, 3L));
            });
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールとエンドポイントの関係情報をIDで取得できる")
      void findRoleEndpointPermissionById() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/1")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(RoleEndpointPermission.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                      RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId,
                      RoleEndpointPermission::getCreatedBy)
                  .containsExactly(1L, 1L, 1L, 1L, 1L);
            });
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
      @DisplayName("ロールとエンドポイントの関係情報を新規登録できる")
      void insertRoleEndpointPermission() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/role-endpoint-permissions")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "roleId": 3,
                  "endpointId": 1
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody(RoleEndpointPermission.class)
            .consumeWith(response -> assertThat(response.getResponseBody())
                .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                    RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId,
                    RoleEndpointPermission::getCreatedBy)
                .containsExactly(4L, 1L, 3L, 1L, 2L));
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/4")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(RoleEndpointPermission.class)
            .consumeWith(response -> assertThat(response.getResponseBody())
                .extracting(RoleEndpointPermission::getId, RoleEndpointPermission::getNamespaceId,
                    RoleEndpointPermission::getRoleId, RoleEndpointPermission::getEndpointId,
                    RoleEndpointPermission::getCreatedBy)
                .containsExactly(4L, 1L, 3L, 1L, 2L));
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
            .uri("/rbac-service/v1/role-endpoint-permissions")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "roleId": 1,
                  "endpointId": 1
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
                        "org.example.error.exception.RedundantException: RoleEndpointPermission already exists",
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
      @DisplayName("ロールとエンドポイントの関係情報をIDで削除できる")
      void deleteTargetRoleEndpointPermissionById() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/role-endpoint-permissions/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
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
      @DisplayName("ロールとエンドポイントの関係情報をユニークキーで削除できる")
      void deleteTargetRoleEndpointPermissionByUniqueKeys() {
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/role-endpoint-permissions?namespace-id=2&role-id=3&endpoint-id=3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(Void.class);
        webTestClient.get()
            .uri("/rbac-service/v1/role-endpoint-permissions/3")
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Void.class);
      }
    }
  }
}
