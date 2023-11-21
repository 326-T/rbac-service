package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.TargetClusterBelonging;
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
class TargetBelongsGroupsRepositoryTest {

  @Autowired
  private TargetClusterBelongingRepository targetClusterBelongingRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットとグループの関係情報の件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = targetClusterBelongingRepository.count();
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
      @DisplayName("ターゲットとグループの関係情報を全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<TargetClusterBelonging> targetBelongsGroupFlux = targetClusterBelongingRepository.findAll();
        // then
        StepVerifier.create(targetBelongsGroupFlux)
            .assertNext(
                targetClusterBelonging -> assertThat(targetClusterBelonging)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L))
            .assertNext(
                targetClusterBelonging -> assertThat(targetClusterBelonging)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, 2L))
            .assertNext(
                targetClusterBelonging -> assertThat(targetClusterBelonging)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
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
      @DisplayName("ターゲットとグループの関係情報をIDで取得できる")
      void findTargetById() {
        // when
        Mono<TargetClusterBelonging> targetBelongsGroupMono = targetClusterBelongingRepository.findById(
            1L);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging -> assertThat(targetClusterBelonging)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
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
      @DisplayName("ターゲットとグループの関係情報を更新できる")
      void updateTargetBelongsGroup() {
        // given
        TargetClusterBelonging targetClusterBelonging = TargetClusterBelonging.builder()
            .id(2L)
            .targetId(1L)
            .clusterId(2L)
            .createdBy(3L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<TargetClusterBelonging> targetBelongsGroupMono = targetClusterBelongingRepository.save(
            targetClusterBelonging);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
                    .containsExactly(2L, 1L, 2L, 3L))
            .verifyComplete();
        targetClusterBelongingRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
                    .containsExactly(2L, 1L, 2L, 3L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ターゲットとグループの関係情報を新規登録できる")
      void insertTargetBelongsGroup() {
        // given
        TargetClusterBelonging targetClusterBelonging = TargetClusterBelonging.builder()
            .targetId(3L)
            .clusterId(1L)
            .createdBy(1L)
            .build();
        // when
        Mono<TargetClusterBelonging> targetBelongsGroupMono = targetClusterBelongingRepository.save(
            targetClusterBelonging);
        // then
        StepVerifier.create(targetBelongsGroupMono)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
                    .containsExactly(4L, 3L, 1L, 1L))
            .verifyComplete();
        targetClusterBelongingRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                targetClusterBelonging1 -> assertThat(targetClusterBelonging1)
                    .extracting(TargetClusterBelonging::getId, TargetClusterBelonging::getTargetId,
                        TargetClusterBelonging::getClusterId, TargetClusterBelonging::getCreatedBy)
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
      @DisplayName("ターゲットとグループの関係情報をIDで削除できる")
      void deleteTargetBelongsGroupById() {
        // when
        Mono<Void> voidMono = targetClusterBelongingRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        targetClusterBelongingRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}