package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Endpoint;
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
class EndpointRepositoryTest {

  @Autowired
  private EndpointRepository endpointRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("エンドポイントの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = endpointRepository.count();
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
      @DisplayName("エンドポイントを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Endpoint> roleRestPermissionFlux = endpointRepository.findAll();
        // then
        StepVerifier.create(roleRestPermissionFlux)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, "GET", 1L, 1L))
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 2L, "POST", 2L, 2L))
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(3L, 3L, "PUT", 3L, 3L))
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
      @DisplayName("エンドポイントをIDで取得できる")
      void findTargetById() {
        // when
        Mono<Endpoint> roleRestPermissionMono = endpointRepository.findById(
            1L);
        // then
        StepVerifier.create(roleRestPermissionMono)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, "GET", 1L, 1L))
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
      @DisplayName("エンドポイントを更新できる")
      void updateRoleRestPermission() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(2L)
            .pathId(3L)
            .method("GET")
            .targetGroupId(2L)
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Endpoint> roleRestPermissionMono = endpointRepository.save(
            endpoint);
        // then
        StepVerifier.create(roleRestPermissionMono)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 3L, "GET", 2L, 1L))
            .verifyComplete();
        endpointRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 3L, "GET", 2L, 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("エンドポイントを新規登録できる")
      void insertRoleRestPermission() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .pathId(1L)
            .method("DELETE")
            .targetGroupId(2L)
            .createdBy(3L)
            .build();
        // when
        Mono<Endpoint> roleRestPermissionMono = endpointRepository.save(
            endpoint);
        // then
        StepVerifier.create(roleRestPermissionMono)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(4L, 1L, "DELETE", 2L, 3L))
            .verifyComplete();
        endpointRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(4L, 1L, "DELETE", 2L, 3L))
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
      @DisplayName("エンドポイントをIDで削除できる")
      void deleteRoleRestPermissionById() {
        // when
        Mono<Void> voidMono = endpointRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        endpointRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}