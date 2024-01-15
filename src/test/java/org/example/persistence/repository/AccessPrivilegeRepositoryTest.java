package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.persistence.dto.AccessPrivilege;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataR2dbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class AccessPrivilegeRepositoryTest {

  @Autowired
  private AccessPrivilegeRepository accessPrivilegeRepository;

  @Order(1)
  @Nested
  class findByNamespace {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペース内の権限の一覧を全件取得できる")
      void findByNamespace() {
        // when
        Flux<AccessPrivilege> accessPrivilegeFlux = accessPrivilegeRepository.findByNamespace(1L);
        // then
        StepVerifier.create(accessPrivilegeFlux)
            .assertNext(
                accessPrivilege -> assertThat(accessPrivilege)
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
                        2L, "user1",
                        1L, "develop",
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

  @Order(1)
  @Nested
  class findByUserAndNamespace {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーが持つネームスペース内の権限の一覧を全件取得できる")
      void findByUser() {
        // when
        Flux<AccessPrivilege> accessPrivilegeFlux = accessPrivilegeRepository.findByUserAndNamespace(2L, 1L);
        // then
        StepVerifier.create(accessPrivilegeFlux)
            .assertNext(
                accessPrivilege -> assertThat(accessPrivilege)
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
                        2L, "user1",
                        1L, "develop",
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
}