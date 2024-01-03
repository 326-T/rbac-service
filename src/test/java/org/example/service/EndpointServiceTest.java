package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.repository.EndpointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class EndpointServiceTest {

  @InjectMocks
  private EndpointService endpointService;
  @Mock
  private EndpointRepository endpointRepository;

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを名前空間IDで取得できる")
      void findAllTheIndexes() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L)
            .build();
        Endpoint endpoint2 = Endpoint.builder()
            .id(2L).namespaceId(1L).pathId(2L).method("POST").targetGroupId(2L).createdBy(2L)
            .build();
        Endpoint endpoint3 = Endpoint.builder()
            .id(3L).namespaceId(1L).pathId(3L).method("PUT").targetGroupId(3L).createdBy(3L)
            .build();
        when(endpointRepository.findByNamespaceId(1L)).thenReturn(Flux.just(endpoint1, endpoint2, endpoint3));
        // when
        Flux<Endpoint> clusterFlux = endpointService.findByNamespaceId(1L);
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(2L, 1L, 2L, "POST", 2L, 2L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(3L, 1L, 3L, "PUT", 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindByNamespaceIdAndRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを名前空間IDとロールIDで取得できる")
      void canFindByNamespaceIdAndRoleId() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L)
            .build();
        Endpoint endpoint2 = Endpoint.builder()
            .id(2L).namespaceId(1L).pathId(2L).method("POST").targetGroupId(2L).createdBy(2L)
            .build();
        Endpoint endpoint3 = Endpoint.builder()
            .id(3L).namespaceId(1L).pathId(3L).method("PUT").targetGroupId(3L).createdBy(3L)
            .build();
        when(endpointRepository.findByNamespaceIdAndRoleId(1L, 1L))
            .thenReturn(Flux.just(endpoint1, endpoint2, endpoint3));
        // when
        Flux<Endpoint> clusterFlux = endpointService.findByNamespaceIdAndRoleId(1L, 1L);
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(2L, 1L, 2L, "POST", 2L, 2L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(3L, 1L, 3L, "PUT", 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを登録できる")
      void insertTheIndex() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L).build();
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(Mono.just(endpoint1));
        when(endpointRepository.findDuplicate(1L, 1L, 1L, "GET")).thenReturn(Mono.empty());
        // when
        Mono<Endpoint> clusterMono = endpointService.insert(endpoint1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(null, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicateEndpoint() {
        // given
        Endpoint before = Endpoint.builder()
            .namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L).build();
        Endpoint after = Endpoint.builder()
            .namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L).build();
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(Mono.just(after));
        when(endpointRepository.findDuplicate(1L, 1L, 1L, "GET")).thenReturn(Mono.just(before));
        // when
        Mono<Endpoint> clusterMono = endpointService.insert(after);
        // then
        StepVerifier.create(clusterMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを更新できる")
      void updateTheIndex() {
        // given
        Endpoint before = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(2L).method("POST")
            .targetGroupId(2L).createdBy(2L).build();
        Endpoint after = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(3L).method("GET")
            .targetGroupId(3L).createdBy(2L).build();
        when(endpointRepository.findById(2L)).thenReturn(Mono.just(before));
        when(endpointRepository.findDuplicate(2L, 3L, 3L, "GET")).thenReturn(Mono.empty());
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(Mono.just(after));
        // when
        Mono<Endpoint> clusterMono = endpointService.update(after);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                    Endpoint::getPathId, Endpoint::getMethod,
                    Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                .containsExactly(2L, 2L, 3L, "GET", 3L, 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないエンドポイントの場合はエラーになる")
      void notExistingEndpointCauseException() {
        // given
        Endpoint after = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(3L).method("GET")
            .targetGroupId(3L).createdBy(2L).build();
        when(endpointRepository.findById(2L)).thenReturn(Mono.empty());
        when(endpointRepository.findDuplicate(2L, 3L, 3L, "GET")).thenReturn(Mono.empty());
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(Mono.just(after));
        // when
        Mono<Endpoint> clusterMono = endpointService.update(after);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }
    }

    @Test
    @DisplayName("namespaceIdが一致しない場合はエラーになる")
    void cannotUpdateWithDifferentNamespaceId() {
      // given
      Endpoint before = Endpoint.builder()
          .id(2L).namespaceId(2L).pathId(2L).method("POST")
          .targetGroupId(2L).createdBy(2L).build();
      Endpoint after = Endpoint.builder()
          .id(2L).namespaceId(999L).pathId(3L).method("GET")
          .targetGroupId(3L).createdBy(2L).build();
      when(endpointRepository.findById(2L)).thenReturn(Mono.just(before));
      when(endpointRepository.findDuplicate(2L, 3L, 3L, "GET")).thenReturn(Mono.empty());
      when(endpointRepository.save(any(Endpoint.class))).thenReturn(Mono.just(after));
      // when
      Mono<Endpoint> clusterMono = endpointService.update(after);
      // then
      StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
    }

    @Test
    @DisplayName("すでに登録済みの場合はエラーになる")
    void cannotUpdateWithDuplicateEndpoint() {
      // given
      Endpoint before = Endpoint.builder()
          .id(2L).namespaceId(2L).pathId(2L).method("POST")
          .targetGroupId(2L).createdBy(2L).build();
      Endpoint after = Endpoint.builder()
          .id(2L).namespaceId(2L).pathId(3L).method("PUT")
          .targetGroupId(3L).createdBy(3L).build();
      Endpoint duplicate = Endpoint.builder()
          .id(2L).namespaceId(2L).pathId(3L).method("PUT")
          .targetGroupId(3L).createdBy(3L).build();
      when(endpointRepository.findById(2L)).thenReturn(Mono.just(before));
      when(endpointRepository.findDuplicate(2L, 3L, 3L, "PUT")).thenReturn(Mono.just(duplicate));
      when(endpointRepository.save(any(Endpoint.class))).thenReturn(Mono.just(after));
      // when
      Mono<Endpoint> clusterMono = endpointService.update(after);
      // then
      StepVerifier.create(clusterMono).expectError(RedundantException.class).verify();
    }
  }

  @Nested
  class Delete {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを削除できる")
      void deleteTheIndex() {
        // given
        Endpoint endpoint = Endpoint.builder().id(1L).namespaceId(1L).createdBy(1L).build();
        when(endpointRepository.findById(1L)).thenReturn(Mono.just(endpoint));
        when(endpointRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = endpointService.deleteById(1L, 1L);
        // then
        StepVerifier.create(clusterMono).verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないidの場合はエラーになる")
      void notExistingIdCauseException() {
        // given
        when(endpointRepository.findById(1L)).thenReturn(Mono.empty());
        when(endpointRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = endpointService.deleteById(1L, 1L);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotDeleteWithDifferentNamespaceId() {
        // given
        Endpoint endpoint = Endpoint.builder().id(1L).namespaceId(1L).createdBy(1L).build();
        when(endpointRepository.findById(1L)).thenReturn(Mono.just(endpoint));
        when(endpointRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = endpointService.deleteById(1L, 999L);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }
    }
  }
}