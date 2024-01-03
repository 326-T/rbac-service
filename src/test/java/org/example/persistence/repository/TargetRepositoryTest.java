package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Target;
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
class TargetRepositoryTest {

  @Autowired
  private TargetRepository targetRepository;

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットをIDで取得できる")
      void findUserById() {
        // when
        Mono<Target> targetMono = targetRepository.findById(1L);
        // then
        StepVerifier.create(targetMono).assertNext(
            target -> assertThat(target).extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(1L, 1L, "object-id-1", 1L)).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class findByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットをnamespaceIdで全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Target> targetFlux = targetRepository.findByNamespaceId(1L);
        // then
        StepVerifier.create(targetFlux)
            .assertNext(target ->
                assertThat(target)
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex, Target::getCreatedBy)
                    .containsExactly(1L, 1L, "object-id-1", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByNamespaceIdAndTargetGroupId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットをnamespaceIdとtargetGroupIdで全件取得できる")
      void canFindByNamespaceIdAndTargetGroupId() {
        // when
        Flux<Target> targetFlux = targetRepository.findByNamespaceIdAndTargetGroupId(2L, 2L);
        // then
        StepVerifier.create(targetFlux)
            .assertNext(target ->
                assertThat(target)
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex, Target::getCreatedBy)
                    .containsExactly(2L, 2L, "object-id-2", 2L));
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
      @DisplayName("ターゲットを更新できる")
      void updateTarget() {
        // given
        Target target = Target.builder().id(2L).namespaceId(1L).objectIdRegex("OBJECT-ID-2")
            .createdBy(1L)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        // when
        Mono<Target> targetMono = targetRepository.save(target);
        // then
        StepVerifier.create(targetMono).assertNext(
            target1 -> assertThat(target1).extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(2L, 1L, "OBJECT-ID-2", 1L)).verifyComplete();
        targetRepository.findById(2L).as(StepVerifier::create).assertNext(
            target1 -> assertThat(target1).extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(2L, 1L, "OBJECT-ID-2", 1L)).verifyComplete();
      }

      @Test
      @DisplayName("ターゲットを新規登録できる")
      void insertTarget() {
        // given
        Target target = Target.builder().namespaceId(1L).objectIdRegex("object-id-4").createdBy(1L)
            .build();
        // when
        Mono<Target> targetMono = targetRepository.save(target);
        // then
        StepVerifier.create(targetMono).assertNext(
            target1 -> assertThat(target1).extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(4L, 1L, "object-id-4", 1L)).verifyComplete();
        targetRepository.findById(4L).as(StepVerifier::create).assertNext(
            target1 -> assertThat(target1).extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(4L, 1L, "object-id-4", 1L)).verifyComplete();
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
      @DisplayName("ターゲットをIDで削除できる")
      void deleteTargetById() {
        // when
        Mono<Void> voidMono = targetRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        targetRepository.findById(3L).as(StepVerifier::create).verifyComplete();
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
      @DisplayName("ターゲットの重複を検知できる")
      void findDuplicateTarget() {
        // when
        Mono<Target> targetMono = targetRepository.findDuplicate(1L, "object-id-1");
        // then
        StepVerifier.create(targetMono).assertNext(
            target -> assertThat(target).extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(1L, 1L, "object-id-1", 1L)).verifyComplete();
      }
    }
  }
}