package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.example.persistence.dto.AccessPrivilege;
import org.example.persistence.repository.AccessPrivilegeRepository;
import org.example.web.request.AccessPrivilegeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class AccessPrivilegeServiceTest {

  @InjectMocks
  private AccessPrivilegeService accessPrivilegeService;
  @Mock
  private AccessPrivilegeRepository accessPrivilegeRepository;

  @Nested
  class findByNamespace {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("指定した名前空間のアクセス権を全件取得できる")
      void findAllTheAccessPrivileges() {
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
        when(accessPrivilegeRepository.findByNamespace(1L)).thenReturn(Flux.just(accessPrivilege));
        // when
        Flux<AccessPrivilege> accessPrivilegeFlux = accessPrivilegeService.findByNamespace(1L);
        // then
        StepVerifier.create(accessPrivilegeFlux)
            .assertNext(access -> assertThat(access)
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
                    1L, "user1",
                    1L, "developers",
                    1L, "group1",
                    1L, "developers",
                    1L, "/user-service/v1/",
                    "GET",
                    1L, "target-group-1",
                    1L, "object-id-1"
                ))
            .verifyComplete();
      }
    }
  }

  @Nested
  class canAccess {

    @Nested
    @DisplayName("正常系")
    class regular {

      @ParameterizedTest
      @CsvSource({
          "GET, '/user-service/v1/', object-id-1",
          "POST, '/user-service/v1/', object-id-1",
          "GET, '/user-service/v1/', object-id-2",
          "GET, '/user-service/v1/', object-id-3",
          "GET, '/user-service/v1/login/', object-id-1"
      })
      @DisplayName("ユーザがある名前空間のリソースにアクセスできるか確認し権限があればtrueを返す")
      void canAccess(String method, String path, String objectId) {
        // given
        AccessPrivilege accessPrivilege = AccessPrivilege.builder()
            .userId(1L).userName("user1")
            .namespaceId(1L).namespaceName("developers")
            .userGroupId(1L).userGroupName("group1")
            .roleId(1L).roleName("developers")
            .pathId(1L).pathRegex("/user-service/v1/.*")
            .targetGroupId(1L).targetGroupName("target-group-1")
            .targetId(1L).objectIdRegex("object-id-[1-3]")
            .method("(GET|POST)")
            .build();
        AccessPrivilegeRequest ask = new AccessPrivilegeRequest();
        ask.setUserId(1L);
        ask.setNamespaceId(1L);
        ask.setMethod(method);
        ask.setPath(path);
        ask.setObjectId(objectId);
        when(accessPrivilegeRepository.findByUser(1L)).thenReturn(Flux.just(accessPrivilege));
        // when
        Mono<Boolean> canAccess = accessPrivilegeService.canAccess(ask);
        // then
        StepVerifier.create(canAccess)
            .expectNext(true)
            .verifyComplete();
      }

      @ParameterizedTest
      @CsvSource({
          "2L, GET, '/user-service/v1/', object-id-1",
          "1L, DELETE, '/user-service/v1/', object-id-1",
          "1L, GET, '/user-service/v2/', object-id-1",
          "1L, GET, '/user-service/v1/', object-id-4"
      })
      @DisplayName("権限がない場合はfalseを返す")
      void cannotAccess(String method, String path, String objectId) {
        // given
        AccessPrivilege accessPrivilege = AccessPrivilege.builder()
            .userId(1L).userName("user1")
            .namespaceId(1L).namespaceName("developers")
            .userGroupId(1L).userGroupName("group1")
            .roleId(1L).roleName("developers")
            .pathId(1L).pathRegex("/user-service/v1/.*")
            .targetGroupId(1L).targetGroupName("target-group-1")
            .targetId(1L).objectIdRegex("object-id-[1-3]")
            .method("(GET|POST)")
            .build();
        AccessPrivilegeRequest ask = new AccessPrivilegeRequest();
        ask.setUserId(1L);
        ask.setNamespaceId(1L);
        ask.setMethod(method);
        ask.setPath(path);
        ask.setObjectId(objectId);
        when(accessPrivilegeRepository.findByUser(1L)).thenReturn(Flux.just(accessPrivilege));
        // when
        Mono<Boolean> canAccess = accessPrivilegeService.canAccess(ask);
        // then
        StepVerifier.create(canAccess)
            .expectNext(false)
            .verifyComplete();
      }
    }
  }
}