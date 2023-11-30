package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントの件数を取得できる")
      void countTheIndexes() {
        // given
        when(endpointRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = endpointService.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Nested
  class FindAll {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントを全件取得できる")
      void findAllTheIndexes() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L)
            .build();
        Endpoint endpoint2 = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(2L).method("POST").targetGroupId(2L).createdBy(2L)
            .build();
        Endpoint endpoint3 = Endpoint.builder()
            .id(3L).namespaceId(3L).pathId(3L).method("PUT").targetGroupId(3L).createdBy(3L)
            .build();
        when(endpointRepository.findAll()).thenReturn(Flux.just(endpoint1, endpoint2,
            endpoint3));
        // when
        Flux<Endpoint> clusterFlux = endpointService.findAll();
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, "POST", 2L, 2L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(3L, 3L, 3L, "PUT", 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントをIDで取得できる")
      void findByIdTheIndex() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L)
            .build();
        when(endpointRepository.findById(1L)).thenReturn(Mono.just(endpoint1));
        // when
        Mono<Endpoint> clusterMono = endpointService.findById(1L);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントを登録できる")
      void insertTheIndex() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L).build();
        when(endpointRepository.save(any(Endpoint.class))).thenReturn(
            Mono.just(endpoint1));
        // when
        Mono<Endpoint> clusterMono = endpointService.insert(endpoint1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(null, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントを更新できる")
      void updateTheIndex() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET")
            .targetGroupId(1L).createdBy(1L).build();
        when(endpointRepository.findById(1L)).thenReturn(Mono.just(endpoint1));
        when(endpointRepository.save(any(Endpoint.class)))
            .thenReturn(Mono.just(endpoint1));
        // when
        Mono<Endpoint> clusterMono = endpointService.update(endpoint1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("エンドポイントを削除できる")
      void deleteTheIndex() {
        // given
        when(endpointRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = endpointService.deleteById(1L);
        // then
        StepVerifier.create(clusterMono).verifyComplete();
      }
    }
  }
}