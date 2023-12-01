package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Target;
import org.example.persistence.repository.TargetRepository;
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
class TargetServiceTest {

  @InjectMocks
  private TargetService targetService;
  @Mock
  private TargetRepository targetRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットの件数を取得できる")
      void countTheIndexes() {
        // given
        when(targetRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = targetService.count();
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
      @DisplayName("ターゲットを全件取得できる")
      void findAllTheIndexes() {
        // given
        Target target1 = Target.builder()
            .id(1L).namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        Target target2 = Target.builder()
            .id(2L).namespaceId(2L).objectIdRegex("object-id-2").createdBy(2L).build();
        Target target3 = Target.builder()
            .id(3L).namespaceId(3L).objectIdRegex("object-id-3").createdBy(3L).build();
        when(targetRepository.findAll()).thenReturn(Flux.just(target1, target2,
            target3));
        // when
        Flux<Target> clusterFlux = targetService.findAll();
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(1L, 1L, "object-id-1", 1L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(2L, 2L, "object-id-2", 2L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(3L, 3L, "object-id-3", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットをIDで取得できる")
      void findByIdTheIndex() {
        // given
        Target target1 = Target.builder()
            .id(1L).namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        when(targetRepository.findById(1L)).thenReturn(Mono.just(target1));
        // when
        Mono<Target> clusterMono = targetService.findById(1L);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(1L, 1L, "object-id-1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットを登録できる")
      void insertTheIndex() {
        // given
        Target target1 = Target.builder()
            .namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        when(targetRepository.save(any(Target.class))).thenReturn(
            Mono.just(target1));
        // when
        Mono<Target> clusterMono = targetService.insert(target1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(null, 1L, "object-id-1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットを更新できる")
      void updateTheIndex() {
        // given
        Target before = Target.builder()
            .id(2L).namespaceId(2L).objectIdRegex("object-id-2").createdBy(2L).build();
        Target after = Target.builder()
            .id(2L).namespaceId(2L).objectIdRegex("object-id-1").createdBy(2L).build();
        when(targetRepository.findById(2L)).thenReturn(Mono.just(before));
        when(targetRepository.save(any(Target.class))).thenReturn(Mono.just(after));
        // when
        Mono<Target> clusterMono = targetService.update(after);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(Target::getId, Target::getNamespaceId,
                    Target::getObjectIdRegex, Target::getCreatedBy)
                .containsExactly(2L, 2L, "object-id-1", 2L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットを削除できる")
      void deleteTheIndex() {
        // given
        when(targetRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = targetService.deleteById(1L);
        // then
        StepVerifier.create(clusterMono).verifyComplete();
      }
    }
  }
}