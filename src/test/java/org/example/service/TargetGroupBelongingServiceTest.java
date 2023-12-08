package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetGroupBelonging;
import org.example.persistence.repository.TargetGroupBelongingRepository;
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
class TargetGroupBelongingServiceTest {

  @InjectMocks
  private TargetGroupBelongingService targetGroupBelongingService;
  @Mock
  private TargetGroupBelongingRepository targetGroupBelongingRepository;

  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(targetGroupBelongingRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = targetGroupBelongingService.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        TargetGroupBelonging targetGroupBelonging1 = TargetGroupBelonging.builder()
            .id(1L).namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        TargetGroupBelonging targetGroupBelonging2 = TargetGroupBelonging.builder()
            .id(2L).namespaceId(2L).targetId(2L).targetGroupId(2L).createdBy(2L).build();
        TargetGroupBelonging targetGroupBelonging3 = TargetGroupBelonging.builder()
            .id(3L).namespaceId(3L).targetId(3L).targetGroupId(3L).createdBy(3L).build();
        when(targetGroupBelongingRepository.findAll()).thenReturn(
            Flux.just(targetGroupBelonging1, targetGroupBelonging2,
                targetGroupBelonging3));
        // when
        Flux<TargetGroupBelonging> groupFlux = targetGroupBelongingService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(group -> assertThat(group)
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
            .assertNext(group -> assertThat(group)
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(2L, 2L, 2L, 2L, 2L))
            .assertNext(group -> assertThat(group)
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(3L, 3L, 3L, 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループをIDで取得できる")
      void findByIdTheIndex() {
        // given
        TargetGroupBelonging targetGroupBelonging1 = TargetGroupBelonging.builder()
            .id(1L).namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        when(targetGroupBelongingRepository.findById(1L)).thenReturn(
            Mono.just(targetGroupBelonging1));
        // when
        Mono<TargetGroupBelonging> groupMono = targetGroupBelongingService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを登録できる")
      void insertTheIndex() {
        // given
        TargetGroupBelonging targetGroupBelonging1 = TargetGroupBelonging.builder()
            .namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        when(targetGroupBelongingRepository.save(any(TargetGroupBelonging.class)))
            .thenReturn(Mono.just(targetGroupBelonging1));
        when(targetGroupBelongingRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<TargetGroupBelonging> groupMono = targetGroupBelongingService.insert(
            targetGroupBelonging1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                    TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId,
                    TargetGroupBelonging::getCreatedBy)
                .containsExactly(null, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class irregular {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicateTargetGroupBelonging() {
        // given
        TargetGroupBelonging before = TargetGroupBelonging.builder()
            .namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        TargetGroupBelonging after = TargetGroupBelonging.builder()
            .namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        when(targetGroupBelongingRepository.save(any(TargetGroupBelonging.class)))
            .thenReturn(Mono.just(after));
        when(targetGroupBelongingRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.just(before));
        // when
        Mono<TargetGroupBelonging> groupMono = targetGroupBelongingService.insert(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを削除できる")
      void deleteTheIndex() {
        // given
        when(targetGroupBelongingRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = targetGroupBelongingService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}