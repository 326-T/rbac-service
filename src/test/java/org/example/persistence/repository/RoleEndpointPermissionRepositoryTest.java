package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.RoleEndpointPermission;
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
class RoleEndpointPermissionRepositoryTest {

  @Autowired
  private RoleEndpointPermissionRepository roleEndpointPermissionRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = roleEndpointPermissionRepository.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<RoleEndpointPermission> groupHasRoleFlux = roleEndpointPermissionRepository.findAll();
        // then
        StepVerifier.create(groupHasRoleFlux)
            .assertNext(
                roleEndpointPermission -> assertThat(roleEndpointPermission)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .assertNext(
                roleEndpointPermission -> assertThat(roleEndpointPermission)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, 2L, 2L))
            .assertNext(
                roleEndpointPermission -> assertThat(roleEndpointPermission)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(3L, 3L, 3L, 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報をIDで取得できる")
      void findUserById() {
        // when
        Mono<RoleEndpointPermission> groupHasRoleMono = roleEndpointPermissionRepository.findById(
            1L);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                roleEndpointPermission -> assertThat(roleEndpointPermission)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
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
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報を更新できる")
      void updateGroupHasRole() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .id(2L)
            .namespaceId(3L)
            .roleId(1L)
            .endpointId(2L)
            .createdBy(3L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<RoleEndpointPermission> groupHasRoleMono = roleEndpointPermissionRepository.save(
            roleEndpointPermission);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                roleEndpointPermission1 -> assertThat(roleEndpointPermission1)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(2L, 3L, 1L, 2L, 3L))
            .verifyComplete();
        roleEndpointPermissionRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                roleEndpointPermission1 -> assertThat(roleEndpointPermission1)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(2L, 3L, 1L, 2L, 3L))
            .verifyComplete();
      }

      @Test
      @DisplayName("グループとロールの関係情報を新規登録できる")
      void insertGroupHasRole() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .namespaceId(1L)
            .roleId(3L)
            .endpointId(1L)
            .createdBy(1L)
            .build();
        // when
        Mono<RoleEndpointPermission> groupHasRoleMono = roleEndpointPermissionRepository.save(
            roleEndpointPermission);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                roleEndpointPermission1 -> assertThat(roleEndpointPermission1)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(4L, 1L, 3L, 1L, 1L))
            .verifyComplete();
        roleEndpointPermissionRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                roleEndpointPermission1 -> assertThat(roleEndpointPermission1)
                    .extracting(RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(4L, 1L, 3L, 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報をIDで削除できる")
      void deleteGroupHasRoleById() {
        // when
        Mono<Void> voidMono = roleEndpointPermissionRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        roleEndpointPermissionRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindDuplicated {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("グループとロールの関係情報を重複チェックできる")
      void findDuplicated() {
        // when
        Mono<RoleEndpointPermission> roleEndpointPermissionMono = roleEndpointPermissionRepository
            .findDuplicated(1L, 1L, 1L);
        // then
        StepVerifier.create(roleEndpointPermissionMono)
            .assertNext(
                roleEndpointPermission -> assertThat(roleEndpointPermission)
                    .extracting(
                        RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }
}