package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
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
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("クラスターを名前空間IDで取得できる")
      void findAllTheIndexes() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("cluster1").createdBy(1L).build();
        TargetGroup targetGroup2 = TargetGroup.builder()
            .id(2L).namespaceId(1L).name("cluster2").createdBy(2L).build();
        TargetGroup targetGroup3 = TargetGroup.builder()
            .id(3L).namespaceId(1L).name("cluster3").createdBy(3L).build();
        when(targetGroupRepository.findByNamespaceId(1L)).thenReturn(Flux.just(targetGroup1, targetGroup2, targetGroup3));
        // when
        Flux<TargetGroup> clusterFlux = targetGroupService.findByNamespaceId(1L);
        // then
        StepVerifier.create(clusterFlux)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                    TargetGroup::getName, TargetGroup::getCreatedBy)
                .containsExactly(1L, 1L, "cluster1", 1L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                    TargetGroup::getName, TargetGroup::getCreatedBy)
                .containsExactly(2L, 1L, "cluster2", 2L))
            .assertNext(cluster -> assertThat(cluster)
                .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                    TargetGroup::getName, TargetGroup::getCreatedBy)
                .containsExactly(3L, 1L, "cluster3", 3L))
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
      @DisplayName("クラスターを登録できる")
      void insertTheIndex() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(Mono.just(targetGroup1));
        when(targetGroupRepository.findDuplicate(1L, "cluster1"))
            .thenReturn(Mono.empty());
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.insert(targetGroup1);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                    TargetGroup::getName, TargetGroup::getCreatedBy)
                .containsExactly(null, 1L, "cluster1", 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicateTargetGroup() {
        // given
        TargetGroup before = TargetGroup.builder().namespaceId(1L).name("cluster1").createdBy(1L).build();
        TargetGroup after = TargetGroup.builder().namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.findDuplicate(1L, "cluster1"))
            .thenReturn(Mono.just(before));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.insert(after);
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
      @DisplayName("クラスターを更新できる")
      void updateTheIndex() {
        // given
        TargetGroup before = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("cluster2").createdBy(2L).build();
        TargetGroup after = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("CLUSTER2").createdBy(2L).build();
        when(targetGroupRepository.findById(2L)).thenReturn(Mono.just(before));
        when(targetGroupRepository.findDuplicate(2L, "CLUSTER2")).thenReturn(Mono.empty());
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.update(after);
        // then
        StepVerifier.create(clusterMono)
            .assertNext(cluster -> assertThat(cluster)
                .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                    TargetGroup::getName, TargetGroup::getCreatedBy)
                .containsExactly(2L, 2L, "CLUSTER2", 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないターゲットグループの場合はエラーになる")
      void notExistingTargetGroupCauseException() {
        // given
        TargetGroup after = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("CLUSTER2").createdBy(2L).build();
        when(targetGroupRepository.findById(2L)).thenReturn(Mono.empty());
        when(targetGroupRepository.findDuplicate(2L, "CLUSTER2")).thenReturn(Mono.empty());
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.update(after);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotUpdateWithDifferentNamespaceId() {
        // given
        TargetGroup before = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("cluster2").createdBy(2L).build();
        TargetGroup after = TargetGroup.builder()
            .id(2L).namespaceId(999L).name("CLUSTER2").createdBy(2L).build();
        when(targetGroupRepository.findById(2L)).thenReturn(Mono.just(before));
        when(targetGroupRepository.findDuplicate(2L, "CLUSTER2")).thenReturn(Mono.empty());
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.update(after);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // given
        TargetGroup before = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("cluster2").createdBy(2L).build();
        TargetGroup after = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("CLUSTER2").createdBy(2L).build();
        TargetGroup duplicate = TargetGroup.builder()
            .id(3L).namespaceId(2L).name("CLUSTER2").createdBy(2L).build();
        when(targetGroupRepository.findById(2L)).thenReturn(Mono.just(before));
        when(targetGroupRepository.findDuplicate(2L, "CLUSTER2")).thenReturn(Mono.just(duplicate));
        when(targetGroupRepository.save(any(TargetGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<TargetGroup> clusterMono = targetGroupService.update(after);
        // then
        StepVerifier.create(clusterMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("クラスターを削除できる")
      void deleteTheIndex() {
        // given
        TargetGroup targetGroup = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(targetGroupRepository.findById(1L)).thenReturn(Mono.just(targetGroup));
        // when
        Mono<Void> clusterMono = targetGroupService.deleteById(1L, 1L);
        // then
        StepVerifier.create(clusterMono).verifyComplete();
      }

      @Test
      @DisplayName("存在しないidの場合はエラーになる")
      void notExistingIdCauseException() {
        // given
        when(targetGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(targetGroupRepository.findById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> clusterMono = targetGroupService.deleteById(1L, 2L);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotDeleteWithDifferentNamespaceId() {
        // given
        TargetGroup targetGroup = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("cluster1").createdBy(1L).build();
        when(targetGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(targetGroupRepository.findById(1L)).thenReturn(Mono.just(targetGroup));
        // when
        Mono<Void> clusterMono = targetGroupService.deleteById(1L, 999L);
        // then
        StepVerifier.create(clusterMono).expectError(NotExistingException.class).verify();
      }
    }
  }
}