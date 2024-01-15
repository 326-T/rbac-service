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
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class EndpointRepositoryTest {

  @Autowired
  private EndpointRepository endpointRepository;

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Endpoint> endpointFlux = endpointRepository.findAll();
        // then
        StepVerifier.create(endpointFlux)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, "POST", 2L, 2L))
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(3L, 2L, 3L, "PUT", 3L, 3L))
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
      @DisplayName("エンドポイントをIDで取得できる")
      void findTargetById() {
        // when
        Mono<Endpoint> endpointMono = endpointRepository.findById(
            1L);
        // then
        StepVerifier.create(endpointMono)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをnamespaceIdで取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Endpoint> endpointFlux = endpointRepository.findByNamespaceId(1L);
        // then
        StepVerifier.create(endpointFlux)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindByNamespaceIdAndRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをnamespaceIdとroleIdで取得できる")
      void canFindByNamespaceIdAndRoleId() {
        // when
        Flux<Endpoint> endpointFlux = endpointRepository.findByNamespaceIdAndRoleId(2L, 2L);
        // then
        StepVerifier.create(endpointFlux)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, "POST", 2L, 2L))
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
      @DisplayName("エンドポイントを更新できる")
      void updateRoleRestPermission() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(2L)
            .namespaceId(3L)
            .pathId(3L)
            .method("GET")
            .targetGroupId(2L)
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Endpoint> endpointMono = endpointRepository.save(
            endpoint);
        // then
        StepVerifier.create(endpointMono)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 3L, 3L, "GET", 2L, 1L))
            .verifyComplete();
        endpointRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 3L, 3L, "GET", 2L, 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("エンドポイントを新規登録できる")
      void insertRoleRestPermission() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .namespaceId(1L)
            .pathId(1L)
            .method("DELETE")
            .targetGroupId(2L)
            .createdBy(3L)
            .build();
        // when
        Mono<Endpoint> endpointMono = endpointRepository.save(
            endpoint);
        // then
        StepVerifier.create(endpointMono)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(4L, 1L, 1L, "DELETE", 2L, 3L))
            .verifyComplete();
        endpointRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                endpoint1 -> assertThat(endpoint1)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(4L, 1L, 1L, "DELETE", 2L, 3L))
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

  @Order(1)
  @Nested
  class findDuplicate {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを重複チェックできる")
      void findDuplicate() {
        // when
        Mono<Endpoint> booleanMono = endpointRepository.findDuplicate(1L, 1L, 1L, "GET");
        // then
        StepVerifier.create(booleanMono)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }
  }
}