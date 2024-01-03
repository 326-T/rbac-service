package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.UserGroup;
import org.example.persistence.repository.UserGroupRepository;
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
class UserGroupServiceTest {

  @InjectMocks
  private UserGroupService userGroupService;
  @Mock
  private UserGroupRepository userGroupRepository;

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザグループを名前空間IDで取得できる")
      void findAllTheIndexes() {
        // given
        UserGroup userGroup1 = UserGroup.builder()
            .id(1L).namespaceId(1L).name("group1").createdBy(1L).build();
        UserGroup userGroup2 = UserGroup.builder()
            .id(2L).namespaceId(1L).name("group2").createdBy(2L).build();
        UserGroup userGroup3 = UserGroup.builder()
            .id(3L).namespaceId(1L).name("group3").createdBy(3L).build();
        when(userGroupRepository.findByNamespaceId(1L))
            .thenReturn(Flux.just(userGroup1, userGroup2, userGroup3));
        // when
        Flux<UserGroup> groupFlux = userGroupService.findByNamespaceId(1L);
        // then
        StepVerifier.create(groupFlux)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(1L, 1L, "group1", 1L))
            .assertNext(group -> assertThat(group)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(2L, 1L, "group2", 2L))
            .assertNext(group -> assertThat(group)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(3L, 1L, "group3", 3L))
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
      @DisplayName("ユーザグループを登録できる")
      void insertTheIndex() {
        // given
        UserGroup userGroup1 = UserGroup.builder().namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.findDuplicate(1L, "group1")).thenReturn(Mono.empty());
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(Mono.just(userGroup1));
        // when
        Mono<UserGroup> groupMono = userGroupService.insert(userGroup1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(null, 1L, "group1", 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicateUserGroup() {
        // given
        UserGroup before = UserGroup.builder().namespaceId(1L).name("group1").createdBy(1L).build();
        UserGroup after = UserGroup.builder().namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.findDuplicate(1L, "group1")).thenReturn(Mono.just(before));
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<UserGroup> groupMono = userGroupService.insert(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザグループを更新できる")
      void updateTheIndex() {
        // given
        UserGroup before = UserGroup.builder().id(2L).namespaceId(2L).name("group2").createdBy(2L).build();
        UserGroup after = UserGroup.builder().id(2L).namespaceId(2L).name("GROUP2").createdBy(2L).build();
        when(userGroupRepository.findById(2L)).thenReturn(Mono.just(before));
        when(userGroupRepository.findDuplicate(2L, "GROUP2")).thenReturn(Mono.empty());
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<UserGroup> groupMono = userGroupService.update(after);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroup::getId, UserGroup::getNamespaceId,
                    UserGroup::getName, UserGroup::getCreatedBy)
                .containsExactly(2L, 2L, "GROUP2", 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないユーザグループの場合はエラーになる")
      void notExistingUserGroupCauseException() {
        // given
        UserGroup after = UserGroup.builder().id(2L).namespaceId(2L).name("group2").createdBy(2L).build();
        when(userGroupRepository.findById(2L)).thenReturn(Mono.empty());
        when(userGroupRepository.findDuplicate(2L, "GROUP2")).thenReturn(Mono.empty());
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<UserGroup> groupMono = userGroupService.update(after);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotUpdateWithDifferentNamespaceId() {
        // given
        UserGroup before = UserGroup.builder().id(2L).namespaceId(1L).name("group2").createdBy(2L).build();
        UserGroup after = UserGroup.builder().id(2L).namespaceId(999L).name("GROUP2").createdBy(2L).build();
        when(userGroupRepository.findById(2L)).thenReturn(Mono.just(before));
        when(userGroupRepository.findDuplicate(2L, "GROUP2")).thenReturn(Mono.empty());
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<UserGroup> groupMono = userGroupService.update(after);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // given
        UserGroup before = UserGroup.builder().id(2L).namespaceId(2L).name("group2").createdBy(2L).build();
        UserGroup after = UserGroup.builder().id(2L).namespaceId(2L).name("GROUP2").createdBy(2L).build();
        UserGroup duplicate = UserGroup.builder().id(2L).namespaceId(2L).name("GROUP2").createdBy(2L).build();
        when(userGroupRepository.findById(2L)).thenReturn(Mono.just(before));
        when(userGroupRepository.findDuplicate(2L, "GROUP2")).thenReturn(Mono.just(duplicate));
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(Mono.just(after));
        // when
        Mono<UserGroup> groupMono = userGroupService.update(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザグループを削除できる")
      void deleteTheIndex() {
        // given
        UserGroup userGroup = UserGroup.builder().id(1L).namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(userGroup));
        when(userGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userGroupService.deleteById(1L, 1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないidの場合はエラーになる")
      void notExistingIdCauseException() {
        // given
        when(userGroupRepository.findById(1L)).thenReturn(Mono.empty());
        when(userGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> targetMono = userGroupService.deleteById(1L, 1L);
        // then
        StepVerifier.create(targetMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotDeleteWithDifferentNamespaceId() {
        // given
        UserGroup userGroup = UserGroup.builder().id(1L).namespaceId(1L).name("group1").createdBy(1L).build();
        when(userGroupRepository.findById(1L)).thenReturn(Mono.just(userGroup));
        when(userGroupRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> targetMono = userGroupService.deleteById(1L, 999L);
        // then
        StepVerifier.create(targetMono).expectError(NotExistingException.class).verify();
      }
    }
  }
}