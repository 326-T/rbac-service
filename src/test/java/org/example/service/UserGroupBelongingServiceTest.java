package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.User;
import org.example.persistence.entity.UserGroup;
import org.example.persistence.entity.UserGroupBelonging;
import org.example.persistence.repository.UserGroupBelongingRepository;
import org.example.persistence.repository.UserGroupRepository;
import org.example.persistence.repository.UserRepository;
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
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserGroupRepository userGroupRepository;

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
        when(userRepository.findById(1L)).thenReturn(Mono.just(User.builder().id(1L).build()));
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(UserGroup.builder().id(1L).namespaceId(1L).build()));
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
        when(userRepository.findById(1L)).thenReturn(Mono.just(User.builder().id(1L).build()));
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(UserGroup.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.insert(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }

      @Test
      @DisplayName("ユーザが存在しない場合はエラーになる")
      void cannotCreateUserRoleBelongingIfUserDoesNotExist() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingRepository.save(any(UserGroupBelonging.class)))
            .thenReturn(Mono.just(userGroupBelonging));
        when(userGroupBelongingRepository.findDuplicate(1L, 1L, 1L)).thenReturn(Mono.empty());
        when(userRepository.findById(1L)).thenReturn(Mono.empty());
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(UserGroup.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.insert(userGroupBelonging);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("ユーザグループが存在しない場合はエラーになる")
      void cannotCreateUserRoleBelongingIfUserGroupDoesNotExist() {
        // given
        UserGroupBelonging userGroupBelonging = UserGroupBelonging.builder()
            .namespaceId(1L).userId(1L).userGroupId(1L).createdBy(1L).build();
        when(userGroupBelongingRepository.save(any(UserGroupBelonging.class)))
            .thenReturn(Mono.just(userGroupBelonging));
        when(userGroupBelongingRepository.findDuplicate(1L, 1L, 1L)).thenReturn(Mono.empty());
        when(userRepository.findById(1L)).thenReturn(Mono.just(User.builder().id(1L).build()));
        when(userGroupRepository.findById(1L)).thenReturn(Mono.empty());
        // when
        Mono<UserGroupBelonging> groupMono = userGroupBelongingService.insert(userGroupBelonging);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
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