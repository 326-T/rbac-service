package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.TargetGroupBelonging;
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
class TargetGroupBelongingRepositoryTest {

  @Autowired
  private TargetGroupBelongingRepository targetGroupBelongingRepository;

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係情報をIDで取得できる")
      void findTargetById() {
        // when
        Mono<TargetGroupBelonging> targetBelongsGroupMono = targetGroupBelongingRepository.findById(
            1L);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging -> assertThat(targetClusterBelonging)
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                        TargetGroupBelonging::getCreatedBy)
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
      @DisplayName("ターゲットとグループの関係情報を更新できる")
      void updateTargetBelongsGroup() {
        // given
        TargetGroupBelonging targetGroupBelonging = TargetGroupBelonging.builder()
            .id(2L)
            .namespaceId(1L)
            .targetId(1L)
            .targetGroupId(2L)
            .createdBy(3L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<TargetGroupBelonging> targetBelongsGroupMono = targetGroupBelongingRepository.save(
            targetGroupBelonging);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                        TargetGroupBelonging::getCreatedBy)
                    .containsExactly(2L, 1L, 1L, 2L, 3L))
            .verifyComplete();
        targetGroupBelongingRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                        TargetGroupBelonging::getCreatedBy)
                    .containsExactly(2L, 1L, 1L, 2L, 3L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ターゲットとグループの関係情報を新規登録できる")
      void insertTargetBelongsGroup() {
        // given
        TargetGroupBelonging targetGroupBelonging = TargetGroupBelonging.builder()
            .namespaceId(3L)
            .targetId(3L)
            .targetGroupId(1L)
            .createdBy(1L)
            .build();
        // when
        Mono<TargetGroupBelonging> targetBelongsGroupMono = targetGroupBelongingRepository.save(
            targetGroupBelonging);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                        TargetGroupBelonging::getCreatedBy)
                    .containsExactly(4L, 3L, 3L, 1L, 1L))
            .verifyComplete();
        targetGroupBelongingRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                        TargetGroupBelonging::getCreatedBy)
                    .containsExactly(4L, 3L, 3L, 1L, 1L))
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
      @DisplayName("ターゲットとグループの関係情報をユニークキーで削除できる")
      void deleteByUniqueKeys() {
        // when
        Mono<Void> voidMono = targetGroupBelongingRepository.deleteByUniqueKeys(2L, 3L, 3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        targetGroupBelongingRepository.findById(3L).as(StepVerifier::create).verifyComplete();
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
      @DisplayName("ターゲットとグループの関係情報が重複しているかどうかを取得できる")
      void findDuplicate() {
        // when
        Mono<TargetGroupBelonging> targetBelongsGroupMono = targetGroupBelongingRepository.findDuplicate(1L, 1L, 1L);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging -> assertThat(targetClusterBelonging)
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                        TargetGroupBelonging::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }
}