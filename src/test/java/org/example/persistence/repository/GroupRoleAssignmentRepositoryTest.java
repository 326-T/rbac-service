package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.GroupRoleAssignment;
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
class GroupRoleAssignmentRepositoryTest {

  @Autowired
  private GroupRoleAssignmentRepository groupRoleAssignmentRepository;

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
        Mono<Long> count = groupRoleAssignmentRepository.count();
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
        Flux<GroupRoleAssignment> groupHasRoleFlux = groupRoleAssignmentRepository.findAll();
        // then
        StepVerifier.create(groupHasRoleFlux)
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L))
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, 2L))
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(3L, 3L, 3L, 3L))
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
        Mono<GroupRoleAssignment> groupHasRoleMono = groupRoleAssignmentRepository.findById(1L);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment -> assertThat(groupRoleAssignment)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L))
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
        GroupRoleAssignment groupRoleAssignment = GroupRoleAssignment.builder()
            .id(2L)
            .roleId(1L)
            .groupId(2L)
            .createdBy(3L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<GroupRoleAssignment> groupHasRoleMono = groupRoleAssignmentRepository.save(
            groupRoleAssignment);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(2L, 1L, 2L, 3L))
            .verifyComplete();
        groupRoleAssignmentRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(2L, 1L, 2L, 3L))
            .verifyComplete();
      }

      @Test
      @DisplayName("グループとロールの関係情報を新規登録できる")
      void insertGroupHasRole() {
        // given
        GroupRoleAssignment groupRoleAssignment = GroupRoleAssignment.builder()
            .roleId(3L)
            .groupId(1L)
            .createdBy(1L)
            .build();
        // when
        Mono<GroupRoleAssignment> groupHasRoleMono = groupRoleAssignmentRepository.save(
            groupRoleAssignment);
        // then
        StepVerifier.create(groupHasRoleMono)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 3L, 1L, 1L))
            .verifyComplete();
        groupRoleAssignmentRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                groupRoleAssignment1 -> assertThat(groupRoleAssignment1)
                    .extracting(GroupRoleAssignment::getId,
                        GroupRoleAssignment::getRoleId,
                        GroupRoleAssignment::getGroupId,
                        GroupRoleAssignment::getCreatedBy)
                    .containsExactly(4L, 3L, 1L, 1L))
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
        Mono<Void> voidMono = groupRoleAssignmentRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        groupRoleAssignmentRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}