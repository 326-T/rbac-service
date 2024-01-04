package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.UserSystemRolePermission;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class UserSystemRolePermissionRepositoryTest {

  @Autowired
  private UserSystemRolePermissionRepository userSystemRolePermissionRepository;

  @Order(1)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザとシステムロールの紐付けを更新できる")
      void update() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .id(1L)
            .systemRoleId(2L)
            .userId(4L)
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<UserSystemRolePermission> userSystemRolePermissionMono = userSystemRolePermissionRepository.save(userSystemRolePermission);
        // then
        StepVerifier.create(userSystemRolePermissionMono)
            .assertNext(actual ->
                assertThat(actual)
                    .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getUserId,
                        UserSystemRolePermission::getSystemRoleId, UserSystemRolePermission::getCreatedBy)
                    .containsExactly(1L, 4L, 2L, 1L))
            .verifyComplete();
        userSystemRolePermissionRepository.findDuplicate(4L, 2L)
            .as(StepVerifier::create)
            .assertNext(actual ->
                assertThat(actual)
                    .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getUserId,
                        UserSystemRolePermission::getSystemRoleId, UserSystemRolePermission::getCreatedBy)
                    .containsExactly(1L, 4L, 2L, 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ユーザとシステムロールの紐付けを作成できる")
      void insert() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .systemRoleId(1L)
            .userId(4L)
            .createdBy(1L)
            .build();
        // when
        Mono<UserSystemRolePermission> userSystemRolePermissionMono = userSystemRolePermissionRepository.save(userSystemRolePermission);
        // then
        StepVerifier.create(userSystemRolePermissionMono)
            .assertNext(actual ->
                assertThat(actual)
                    .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getUserId,
                        UserSystemRolePermission::getSystemRoleId, UserSystemRolePermission::getCreatedBy)
                    .containsExactly(10L, 4L, 1L, 1L))
            .verifyComplete();
        userSystemRolePermissionRepository.findDuplicate(4L, 1L)
            .as(StepVerifier::create)
            .assertNext(actual ->
                assertThat(actual)
                    .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getUserId,
                        UserSystemRolePermission::getSystemRoleId, UserSystemRolePermission::getCreatedBy)
                    .containsExactly(10L, 4L, 1L, 1L))
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
      @DisplayName("ユーザとシステムロールの紐付けを削除できる")
      void deleteByUniqueKeys() {
        // when
        Mono<Void> voidMono = userSystemRolePermissionRepository
            .deleteByUniqueKeys(2L, 3L, 5L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        userSystemRolePermissionRepository.findDuplicate(3L, 5L)
            .as(StepVerifier::create)
            .verifyComplete();
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
      @DisplayName("重複するユーザとシステムロールの関係情報を検知できる")
      void findDuplicate() {
        // when
        Mono<UserSystemRolePermission> userSystemRolePermissionMono = userSystemRolePermissionRepository.findDuplicate(3L, 5L);
        // then
        StepVerifier.create(userSystemRolePermissionMono)
            .assertNext(actual ->
                assertThat(actual)
                    .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getUserId,
                        UserSystemRolePermission::getSystemRoleId, UserSystemRolePermission::getCreatedBy)
                    .containsExactly(9L, 3L, 5L, 1L))
            .verifyComplete();
      }
    }
  }
}