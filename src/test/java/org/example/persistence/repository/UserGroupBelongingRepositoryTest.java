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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class UserGroupBelongingRepositoryTest {

  @Autowired
  private UserGroupBelongingRepository userGroupBelongingRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザとグループの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = userGroupBelongingRepository.count();
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
      @DisplayName("ユーザとグループの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<UserGroupBelonging> userBelongsGroupFlux = userGroupBelongingRepository.findAll();
        // then
        StepVerifier.create(userBelongsGroupFlux)
            .assertNext(
                userGroupBelonging -> assertThat(userGroupBelonging)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 2L, 1L))
            .assertNext(
                userGroupBelonging -> assertThat(userGroupBelonging)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, 3L, 2L))
            .assertNext(
                userGroupBelonging -> assertThat(userGroupBelonging)
                    .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                        UserGroupBelonging::getUserGroupId, UserGroupBelonging::getUserId,
                        UserGroupBelonging::getCreatedBy)
                    .containsExactly(3L, 3L, 3L, 4L, 3L))
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
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザとグループの関係情報をIDで削除できる")
      void deleteUserBelongsGroupById() {
        // when
        Mono<Void> voidMono = userGroupBelongingRepository.deleteById(3L);
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