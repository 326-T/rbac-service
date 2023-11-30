package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.TargetGroup;
import org.example.persistence.repository.TargetGroupRepository;
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
class TargetGroupServiceTest {

  @InjectMocks
  private TargetGroupService targetGroupService;
  @Mock
  private TargetGroupRepository targetGroupRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("クラスターの件数を取得できる")
      void countTheIndexes() {
        // given
        when(targetGroupRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = targetGroupService.count();
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
      @DisplayName("クラスターを全件取得できる")
      void findAllTheIndexes() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("cluster1").createdBy(1L).build();
        TargetGroup targetGroup2 = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("cluster2").createdBy(2L).build();
        TargetGroup targetGroup3 = TargetGroup.builder()
            .id(3L).namespaceId(3L).name("cluster3").createdBy(3L).build();
        when(targetGroupRepository.findAll()).thenReturn(Flux.just(targetGroup1, targetGroup2,
            targetGroup3));
        // when
        Flux<TargetGroup> clusterFlux = targetGroupService.findAll();
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "cluster1", 1L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(2L, 2L, "cluster2", 2L))
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(3L, 3L, "cluster3", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("クラスターをIDで取得できる")
      void findByIdTheIndex() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.findById(1L)).thenReturn(Mono.just(targetGroup1));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.findById(1L);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "cluster1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("クラスターを登録できる")
      void insertTheIndex() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(
            Mono.just(targetGroup1));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.insert(targetGroup1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(null, 1L, "cluster1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("クラスターを更新できる")
      void updateTheIndex() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.findById(1L)).thenReturn(Mono.just(targetGroup1));
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(
            Mono.just(targetGroup1));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.update(targetGroup1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(
                cluster -> assertThat(cluster)
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "cluster1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("クラスターを削除できる")
      void deleteTheIndex() {
        // given
        when(targetGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = targetGroupService.deleteById(1L);
        // then
        StepVerifier.create(clusterMono).verifyComplete();
      }
    }
  }
}