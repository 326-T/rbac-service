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
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class UserGroupRoleAssignmentRepositoryTest {

  @Autowired
  private UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;

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
  class DeleteByUniqueKeys {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("グループとロールの関係情報をユニークキーで削除できる")
      void canDeleteGroupRoleAssignmentByUniqueKeys() {
        // when
        Mono<Void> voidMono = userGroupRoleAssignmentRepository.deleteByUniqueKeys(2L, 3L, 3L);
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