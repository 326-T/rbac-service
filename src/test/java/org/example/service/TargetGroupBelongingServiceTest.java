package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class TargetGroupBelongingServiceTest {

  @InjectMocks
  private TargetGroupBelongingService targetGroupBelongingService;
  @Mock
  private TargetGroupBelongingRepository targetGroupBelongingRepository;

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

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
    class Error {

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
  class DeleteByUniqueKeys {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループを名前空間IDとターゲットIDとターゲットグループIDで削除できる")
      void deleteByUniqueKeys() {
        // given
        when(targetGroupBelongingRepository.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = targetGroupBelongingService.deleteByUniqueKeys(1L, 1L, 1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
        verify(targetGroupBelongingRepository).deleteByUniqueKeys(1L, 1L, 1L);
      }
    }
  }
}