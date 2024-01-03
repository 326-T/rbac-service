package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.persistence.repository.UserGroupBelongingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class UserGroupBelongingServiceTest {

  @InjectMocks
  private UserGroupBelongingService userGroupBelongingService;
  @Mock
  private UserGroupBelongingRepository userGroupBelongingRepository;

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザグループを登録できる")
      void insertTheIndex() {
        // given
        UserGroupBelonging userGroupBelonging1 = UserGroupBelonging.builder()
            .namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingRepository.save(any(UserGroupBelonging.class)))
            .thenReturn(Mono.just(userGroupBelonging1));
        when(userGroupBelongingRepository.findDuplicate(1L, 1L, 1L)).thenReturn(Mono.empty());
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.insert(userGroupBelonging1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupBelonging::getId, UserGroupBelonging::getNamespaceId,
                    UserGroupBelonging::getUserId, UserGroupBelonging::getUserGroupId,
                    UserGroupBelonging::getCreatedBy)
                .containsExactly(null, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicateUserRoleBelonging() {
        // given
        UserGroupBelonging before = UserGroupBelonging.builder()
            .namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        UserGroupBelonging after = UserGroupBelonging.builder()
            .namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingRepository.save(any(UserGroupBelonging.class)))
            .thenReturn(Mono.just(after));
        when(userGroupBelongingRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.just(before));
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.insert(after);
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
      @DisplayName("ユーザグループを名前空間IDとユーザIDとユーザグループIDで削除できる")
      void deleteByUniqueKeys() {
        // given
        when(userGroupBelongingRepository.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userGroupBelongingService.deleteByUniqueKeys(1L, 1L, 1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
        verify(userGroupBelongingRepository).deleteByUniqueKeys(1L, 1L, 1L);
      }
    }
  }
}