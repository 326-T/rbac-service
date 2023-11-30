package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Path;
import org.example.persistence.repository.PathRepository;
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
class PathServiceTest {

  @InjectMocks
  private PathService pathService;
  @Mock
  private PathRepository pathRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("パスの件数を取得できる")
      void countTheIndexes() {
        // given
        when(pathRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = pathService.count();
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
      @DisplayName("パスを全件取得できる")
      void findAllTheIndexes() {
        // given
        Path path1 = Path.builder()
            .id(1L).namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        Path path2 = Path.builder()
            .id(2L).namespaceId(2L).regex("/billing-service/v1").createdBy(2L).build();
        Path path3 = Path.builder()
            .id(3L).namespaceId(3L).regex("/movie-service/v1").createdBy(3L).build();
        when(pathRepository.findAll()).thenReturn(Flux.just(path1, path2,
            path3));
        // when
        Flux<Path> clusterFlux = pathService.findAll();
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(1L, 1L, "/user-service/v1", 1L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(2L, 2L, "/billing-service/v1", 2L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(3L, 3L, "/movie-service/v1", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("パスをIDで取得できる")
      void findByIdTheIndex() {
        // given
        Path path1 = Path.builder()
            .id(1L).namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        when(pathRepository.findById(1L)).thenReturn(Mono.just(path1));
        // when
        Mono<Path> clusterMono = pathService.findById(1L);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(1L, 1L, "/user-service/v1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("パスを登録できる")
      void insertTheIndex() {
        // given
        Path path1 = Path.builder()
            .namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        when(pathRepository.save(any(Path.class))).thenReturn(
            Mono.just(path1));
        // when
        Mono<Path> clusterMono = pathService.insert(path1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(null, 1L, "/user-service/v1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("パスを更新できる")
      void updateTheIndex() {
        // given
        Path path1 = Path.builder()
            .id(1L).namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        when(pathRepository.findById(1L)).thenReturn(Mono.just(path1));
        when(pathRepository.save(any(Path.class))).thenReturn(
            Mono.just(path1));
        // when
        Mono<Path> clusterMono = pathService.update(path1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(1L, 1L, "/user-service/v1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("パスを削除できる")
      void deleteTheIndex() {
        // given
        when(pathRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = pathService.deleteById(1L);
        // then
        StepVerifier.create(clusterMono).verifyComplete();
      }
    }
  }
}