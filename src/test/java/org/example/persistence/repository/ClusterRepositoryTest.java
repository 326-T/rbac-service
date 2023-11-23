package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Cluster;
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
class ClusterRepositoryTest {

  @Autowired
  private ClusterRepository clusterRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("クラスタの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = clusterRepository.count();
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
      @DisplayName("クラスタを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Cluster> targetGroupFlux = clusterRepository.findAll();
        // then
        StepVerifier.create(targetGroupFlux)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(1L, "target-group-1", 1L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(2L, "target-group-2", 2L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(3L, "target-group-3", 3L))
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
      @DisplayName("クラスタをIDで取得できる")
      void findUserById() {
        // when
        Mono<Cluster> targetGroupMono = clusterRepository.findById(1L);
        // then
        StepVerifier.create(targetGroupMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(1L, "target-group-1", 1L))
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
      @DisplayName("クラスタを更新できる")
      void updateTargetGroup() {
        // given
        Cluster cluster = Cluster.builder()
            .id(2L)
            .name("TARGET-GROUP-2")
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Cluster> targetGroupMono = clusterRepository.save(cluster);
        // then
        StepVerifier.create(targetGroupMono)
            .assertNext(
                cluster1 -> assertThat(cluster1)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(2L, "TARGET-GROUP-2", 1L))
            .verifyComplete();
        clusterRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                cluster1 -> assertThat(cluster1)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(2L, "TARGET-GROUP-2", 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("クラスタを新規登録できる")
      void insertTargetGroup() {
        // given
        Cluster cluster = Cluster.builder()
            .name("target-group-4")
            .createdBy(1L)
            .build();
        // when
        Mono<Cluster> targetGroupMono = clusterRepository.save(cluster);
        // then
        StepVerifier.create(targetGroupMono)
            .assertNext(
                cluster1 -> assertThat(cluster1)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(4L, "target-group-4", 1L))
            .verifyComplete();
        clusterRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                cluster1 -> assertThat(cluster1)
                    .extracting(Cluster::getId, Cluster::getName, Cluster::getCreatedBy)
                    .containsExactly(4L, "target-group-4", 1L))
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
      @DisplayName("クラスタをIDで削除できる")
      void deleteTargetGroupById() {
        // when
        Mono<Void> voidMono = clusterRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        clusterRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}