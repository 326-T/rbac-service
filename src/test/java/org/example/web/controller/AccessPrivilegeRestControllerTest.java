package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.example.persistence.dto.AccessPrivilege;
import org.example.persistence.entity.User;
import org.example.service.AccessPrivilegeService;
import org.example.service.ReactiveContextService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.request.AccessPrivilegeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = AccessPrivilegeRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class AccessPrivilegeRestControllerTest {

  @MockBean
  private AccessPrivilegeService accessPrivilegeService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class findByNamespace {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペース内の権限の一覧を全件取得できる")
      void canGetAllAccessPrivilegesInTheNamespace() {
        // given
        AccessPrivilege accessPrivilege = AccessPrivilege.builder()
            .userId(1L).userName("user1")
            .namespaceId(1L).namespaceName("developers")
            .userGroupId(1L).userGroupName("group1")
            .roleId(1L).roleName("developers")
            .pathId(1L).pathRegex("/user-service/v1/")
            .targetGroupId(1L).targetGroupName("target-group-1")
            .targetId(1L).objectIdRegex("object-id-1")
            .method("GET")
            .build();
        when(accessPrivilegeService.findByNamespace(1L)).thenReturn(Flux.just(accessPrivilege));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/access-privileges?namespace-id=1")
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
                    .containsExactly(tuple(
                        1L, "user1",
                        1L, "developers",
                        1L, "group1",
                        1L, "developers",
                        1L, "/user-service/v1/",
                        "GET",
                        1L, "target-group-1",
                        1L, "object-id-1"
                    ))
            );
      }
    }
  }

  @Nested
  class canAccess {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーが指定したパスにアクセスできるか判定できる")
      void canAccessThePath() {
        // given
        AccessPrivilegeRequest accessPrivilegeRequest = new AccessPrivilegeRequest();
        accessPrivilegeRequest.setPath("/user-service/v1/");
        accessPrivilegeRequest.setMethod("GET");
        when(accessPrivilegeService.canAccess(eq(1L), any(AccessPrivilegeRequest.class)))
            .thenReturn(Mono.just(true));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/access-privileges/can-i")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(accessPrivilegeRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Boolean.class)
            .isEqualTo(true);
      }
    }
  }
}