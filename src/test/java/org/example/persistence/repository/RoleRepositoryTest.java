package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Role;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RoleRepositoryTest {

  @Autowired
  private RoleRepository roleRepository;

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールをIDで取得できる")
      void findUserById() {
        // when
        Mono<Role> roleMono = roleRepository.findById(1L);
        // then
        StepVerifier.create(roleMono)
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developers", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールをnamespaceIdで取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Role> roleFlux = roleRepository.findByNamespaceId(1L);
        // then
        StepVerifier.create(roleFlux)
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developers", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByNamespaceIdAndUserGroupId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールをnamespaceIdとuserGroupIdで取得できる")
      void canFindByNamespaceIdAndUserGroupId() {
        // when
        Flux<Role> roleFlux = roleRepository.findByNamespaceIdAndUserGroupId(2L, 2L);
        // then
        StepVerifier.create(roleFlux)
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(2L, 2L, "operations", 2L))
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
    class Regular {

      @Test
      @DisplayName("ロールを更新できる")
      void updateRole() {
        // given
        Role role = Role.builder()
            .id(2L)
            .namespaceId(1L)
            .name("OPERATIONS")
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Role> roleMono = roleRepository.save(role);
        // then
        StepVerifier.create(roleMono)
            .assertNext(
                role1 -> assertThat(role1)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(2L, 1L, "OPERATIONS", 1L))
            .verifyComplete();
        roleRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                role1 -> assertThat(role1).extracting(Role::getId, Role::getNamespaceId, Role::getName,
                        Role::getCreatedBy)
                    .containsExactly(2L, 1L, "OPERATIONS", 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ロールを新規登録できる")
      void insertRole() {
        // given
        Role role = Role.builder()
            .namespaceId(1L)
            .name("guest")
            .createdBy(1L)
            .build();
        // when
        Mono<Role> roleMono = roleRepository.save(role);
        // then
        StepVerifier.create(roleMono)
            .assertNext(
                role1 -> assertThat(role1)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(4L, 1L, "guest", 1L))
            .verifyComplete();
        roleRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                role1 -> assertThat(role1)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(4L, 1L, "guest", 1L))
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
    class Regular {

      @Test
      @DisplayName("ロールをIDで削除できる")
      void deleteRoleById() {
        // when
        Mono<Void> voidMono = roleRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        roleRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindDuplicate {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロール名が重複しているかどうかを取得できる")
      void findDuplicate() {
        // when
        Mono<Role> roleMono = roleRepository.findDuplicate(1L, "developers");
        // then
        StepVerifier.create(roleMono)
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developers", 1L))
            .verifyComplete();
      }
    }
  }
}