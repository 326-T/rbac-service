package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.UserGroupRoleAssignment;
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
class UserGroupRoleAssignmentRepositoryTest {

  @Autowired
  private UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = userGroupRoleAssignmentRepository.count();
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
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<UserGroupRoleAssignment> groupHasRoleFlux = userGroupRoleAssignmentRepository.findAll();
        // then
        StepVerifier.create(groupHasRoleFlux)
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, 2L, 2L))
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
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
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報をIDで取得できる")
      void findUserById() {
        // when
        Mono<UserGroupRoleAssignment> groupHasRoleMono = userGroupRoleAssignmentRepository.findById(
            1L);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindAllByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報をnamespaceIdで取得できる")
      void findAllTheIndexes() {
        // when
        Flux<UserGroupRoleAssignment> groupHasRoleFlux = userGroupRoleAssignmentRepository.findByNamespaceId(1L);
        // then
        StepVerifier.create(groupHasRoleFlux)
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
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
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報を更新できる")
      void updateGroupHasRole() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .id(2L)
            .namespaceId(1L)
            .roleId(1L)
            .userGroupId(2L)
            .createdBy(3L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<UserGroupRoleAssignment> groupHasRoleMono = userGroupRoleAssignmentRepository.save(
            userGroupRoleAssignment);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(2L, 1L, 1L, 2L, 3L))
            .verifyComplete();
        userGroupRoleAssignmentRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(2L, 1L, 1L, 2L, 3L))
            .verifyComplete();
      }

      @Test
      @DisplayName("グループとロールの関係情報を新規登録できる")
      void insertGroupHasRole() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .namespaceId(1L)
            .roleId(3L)
            .userGroupId(1L)
            .createdBy(1L)
            .build();
        // when
        Mono<UserGroupRoleAssignment> groupHasRoleMono = userGroupRoleAssignmentRepository.save(
            userGroupRoleAssignment);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 1L, 3L, 1L, 1L))
            .verifyComplete();
        userGroupRoleAssignmentRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
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
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報をIDで削除できる")
      void deleteGroupHasRoleById() {
        // when
        Mono<Void> voidMono = userGroupRoleAssignmentRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        userGroupRoleAssignmentRepository.findById(3L).as(StepVerifier::create).verifyComplete();
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
      @DisplayName("重複するグループとロールの関係情報を検知できる")
      void findDuplicateGroupHasRole() {
        // when
        Mono<UserGroupRoleAssignment> groupHasRoleMono = userGroupRoleAssignmentRepository
            .findDuplicate(1L, 1L, 1L);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(UserGroupRoleAssignment::getId,
                        UserGroupRoleAssignment::getNamespaceId,
                        UserGroupRoleAssignment::getRoleId,
                        UserGroupRoleAssignment::getUserGroupId,
                        UserGroupRoleAssignment::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L));
      }
    }
  }
}