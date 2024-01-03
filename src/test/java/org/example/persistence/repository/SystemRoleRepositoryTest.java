package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Namespace;
import org.example.persistence.entity.SystemRole;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class SystemRoleRepositoryTest {

  @Autowired
  private SystemRoleRepository systemRoleRepository;
  @Autowired
  private NamespaceRepository namespaceRepository;

  @Order(1)
  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースIDでシステムロールを取得できる")
      void findByNamespaceId() {
        // when
        Flux<SystemRole> systemRoleFlux = systemRoleRepository.findByNamespaceId(1L);
        // then
        StepVerifier.create(systemRoleFlux)
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        1L, "develop_参照権限",
                        1L, "READ")
            )
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        2L, "develop_編集権限",
                        1L, "WRITE")
            )
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByUserIdAndNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーIDに紐づくシステムロールを取得できる")
      void findByUserId() {
        // when
        Flux<SystemRole> systemRoleFlux = systemRoleRepository.findByUserIdAndNamespaceId(2L, 2L);
        // then
        StepVerifier.create(systemRoleFlux)
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        4L, "staging_編集権限",
                        2L, "WRITE")
            )
            .verifyComplete();
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
      @DisplayName("システムロールを更新できる")
      void updateSystemRole() {
        // given
        SystemRole systemRole = SystemRole.builder()
            .id(3L)
            .name("STAGING_参照権限")
            .namespaceId(2L)
            .permission("READ")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<SystemRole> systemRoleMono = systemRoleRepository.save(systemRole);
        // then
        StepVerifier.create(systemRoleMono)
            .assertNext(savedSystemRole ->
                assertThat(savedSystemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        3L, "STAGING_参照権限",
                        2L, "READ")
            )
            .verifyComplete();
        systemRoleRepository.findById(3L).as(StepVerifier::create)
            .assertNext(savedSystemRole ->
                assertThat(savedSystemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        3L, "STAGING_参照権限",
                        2L, "READ")
            );
      }

      @Test
      @DisplayName("システムロールを新規登録できる")
      void insertSystemRole() {
        // given
        Namespace namespace = Namespace.builder()
            .name("integration")
            .createdBy(1L)
            .build();
        namespaceRepository.save(namespace).block();
        SystemRole systemRole = SystemRole.builder()
            .name("integration_参照権限")
            .namespaceId(4L)
            .permission("READ")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<SystemRole> systemRoleMono = systemRoleRepository.save(systemRole);
        // then
        StepVerifier.create(systemRoleMono)
            .assertNext(savedSystemRole ->
                assertThat(savedSystemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        7L, "integration_参照権限",
                        4L, "READ")
            )
            .verifyComplete();
        systemRoleRepository.findById(7L).as(StepVerifier::create)
            .assertNext(savedSystemRole ->
                assertThat(savedSystemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        7L, "integration_参照権限",
                        4L, "READ")
            );
      }
    }
  }
}