package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.UserGroupBelonging;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class UserGroupBelongingRepositoryTest {

  @Autowired
  private UserGroupBelongingRepository userGroupBelongingRepository;

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザとグループの関係情報をIDで取得できる")
      void findUserById() {
        // when
        Mono<UserGroupBelonging> userBelongsGroupMono = userGroupBelongingRepository.findById(1L);
        // then
        StepVerifier.create(userBelongsGroupMono)
            .assertNext(
                userGroupBelonging -> assertThat(userGroupBelonging)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 2L, 1L))
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
      @DisplayName("ユーザとグループの関係情報を更新できる")
      void updateUserBelongsGroup() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .id(2L)
            .namespaceId(1L)
            .userId(1L)
            .userGroupId(2L)
            .createdBy(3L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<UserGroupBelonging> userBelongsGroupMono = userGroupBelongingRepository.save(
            userGroupBelonging);
        // then
        StepVerifier.create(userBelongsGroupMono)
            .assertNext(
                userGroupBelonging1 -> assertThat(userGroupBelonging1)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(2L, 1L, 2L, 1L, 3L))
            .verifyComplete();
        userGroupBelongingRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                userGroupBelonging1 -> assertThat(userGroupBelonging1)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(2L, 1L, 2L, 1L, 3L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ユーザとグループの関係情報を新規登録できる")
      void insertUserBelongsGroup() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .namespaceId(1L)
            .userId(3L)
            .userGroupId(1L)
            .createdBy(1L)
            .build();
        // when
        Mono<UserGroupBelonging> userBelongsGroupMono = userGroupBelongingRepository.save(
            userGroupBelonging);
        // then
        StepVerifier.create(userBelongsGroupMono)
            .assertNext(
                userGroupBelonging1 -> assertThat(userGroupBelonging1)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(4L, 1L, 1L, 3L, 1L))
            .verifyComplete();
        userGroupBelongingRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                userGroupBelonging1 -> assertThat(userGroupBelonging1)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(4L, 1L, 1L, 3L, 1L))
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
      @DisplayName("ユーザとグループの関係情報をユニークキーで削除できる")
      void deleteByUniqueKeys() {
        // when
        Mono<Void> voidMono = userGroupBelongingRepository.deleteByUniqueKeys(2L, 4L, 3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        userGroupBelongingRepository.findById(3L).as(StepVerifier::create).verifyComplete();
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
      @DisplayName("重複するユーザとグループの関係情報を検知できる")
      void findDuplicateUserGroupBelonging() {
        // when
        Mono<UserGroupBelonging> userBelongsGroupMono = userGroupBelongingRepository.findDuplicate(1L, 2L, 1L);
        // then
        StepVerifier.create(userBelongsGroupMono)
            .assertNext(
                userGroupBelonging -> assertThat(userGroupBelonging)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 2L, 1L))
            .verifyComplete();
      }
    }
  }
}