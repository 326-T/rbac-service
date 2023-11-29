package org.example.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.example.Application;
import org.example.persistence.dto.AccessPrivilege;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@AutoConfigureWebClient
public class AccessPrivilegeAPITest {

  @Autowired
  private WebTestClient webTestClient;

  @Nested
  @Order(1)
  class findByNamespace {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ネームスペース内の権限の一覧を全件取得できる")
      void findByNamespace() {
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/access-privileges?namespaceId=1")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(AccessPrivilege.class)
            .hasSize(1)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(
                        AccessPrivilege::getUserId, AccessPrivilege::getUserName,
                        AccessPrivilege::getNamespaceId, AccessPrivilege::getNamespaceName,
                        AccessPrivilege::getUserGroupId, AccessPrivilege::getUserGroupName,
                        AccessPrivilege::getRoleId, AccessPrivilege::getRoleName,
                        AccessPrivilege::getPathId, AccessPrivilege::getPathRegex,
                        AccessPrivilege::getMethod,
                        AccessPrivilege::getTargetGroupId, AccessPrivilege::getTargetGroupName,
                        AccessPrivilege::getTargetId, AccessPrivilege::getObjectIdRegex
                    )
                    .containsExactly(
                        tuple(1L, "user1",
                            1L, "developers",
                            1L, "group1",
                            1L, "developers",
                            1L, "/user-service/v1/",
                            "GET",
                            1L, "target-group-1",
                            1L, "object-id-1")
                    )
            );
      }
    }
  }

  @Nested
  @Order(1)
  class canAccess {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("アクセス権限を持っているとときtrueを返す")
      void canAccess() {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/access-privileges/can-i")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userId": 1,
                  "namespaceId": 1,
                  "path": "/user-service/v1/",
                  "method": "GET",
                  "objectId": "object-id-1"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class).isEqualTo(true);
      }

      @ParameterizedTest
      @CsvSource({
          "1, 1, /user-service/v1/, GET, object-id-2",
          "1, 1, /user-service/v1/, POST, object-id-1",
          "1, 1, /user-service/v2/, GET, object-id-1",
          "1, 2, /user-service/v1/, GET, object-id-1",
          "2, 1, /user-service/v1/, GET, object-id-1",
      })
      @DisplayName("アクセス権限を持っていないときfalseを返す")
      void canNotAccess(Long userId, Long namespaceId, String path, String method,
          String objectId) {
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/access-privileges/can-i")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "userId": %d,
                  "namespaceId": %d,
                  "path": "%s",
                  "method": "%s",
                  "objectId": "%s"
                }
                """.formatted(userId, namespaceId, path, method, objectId)
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class).isEqualTo(false);
      }
    }
  }
}
