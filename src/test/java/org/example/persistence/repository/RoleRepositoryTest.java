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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RoleRepositoryTest {

  @Autowired
  private RoleRepository roleRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ロールの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = roleRepository.count();
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
      @DisplayName("ロールを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Role> roleFlux = roleRepository.findAll();
        // then
        StepVerifier.create(roleFlux)
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developers", 1L))
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(2L, 2L, "operations", 2L))
            .assertNext(
                role -> assertThat(role)
                    .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                    .containsExactly(3L, 3L, "security", 3L))
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

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    class regular {

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
    class regular {

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
  class FindDuplicated {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ロール名が重複しているかどうかを取得できる")
      void findDuplicated() {
        // when
        Mono<Role> roleMono = roleRepository.findDuplicated(1L, "developers");
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