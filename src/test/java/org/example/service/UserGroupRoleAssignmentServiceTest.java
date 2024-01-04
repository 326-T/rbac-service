package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Role;
import org.example.persistence.entity.UserGroup;
import org.example.persistence.entity.UserGroupRoleAssignment;
import org.example.persistence.repository.RoleRepository;
import org.example.persistence.repository.UserGroupRepository;
import org.example.persistence.repository.UserGroupRoleAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class UserGroupRoleAssignmentServiceTest {

  @InjectMocks
  private UserGroupRoleAssignmentService userGroupRoleAssignmentService;
  @Mock
  private UserGroupRoleAssignmentRepository userGroupRoleAssignmentRepository;
  @Mock
  private UserGroupRepository userGroupRepository;
  @Mock
  private RoleRepository roleRepository;

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループを登録できる")
      void insertTheIndex() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment1 = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.save(any(UserGroupRoleAssignment.class)))
            .thenReturn(Mono.just(userGroupRoleAssignment1));
        when(userGroupRoleAssignmentRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        when(userGroupRepository.findById(1L))
            .thenReturn(Mono.just(UserGroup.builder().id(1L).namespaceId(1L).build()));
        when(roleRepository.findById(1L))
            .thenReturn(Mono.just(Role.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserGroupRoleAssignment> groupMono = userGroupRoleAssignmentService.insert(
            userGroupRoleAssignment1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(UserGroupRoleAssignment::getId,
                    UserGroupRoleAssignment::getNamespaceId,
                    UserGroupRoleAssignment::getUserGroupId,
                    UserGroupRoleAssignment::getRoleId,
                    UserGroupRoleAssignment::getCreatedBy)
                .containsExactly(null, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("ユーザグループとロールの関係を登録できない")
      void cannotCreateDuplicateUserGroupRoleAssignment() {
        // given
        UserGroupRoleAssignment before = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        UserGroupRoleAssignment after = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.save(any(UserGroupRoleAssignment.class)))
            .thenReturn(Mono.just(after));
        when(userGroupRoleAssignmentRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.just(before));
        when(userGroupRepository.findById(1L))
            .thenReturn(Mono.just(UserGroup.builder().id(1L).namespaceId(1L).build()));
        when(roleRepository.findById(1L))
            .thenReturn(Mono.just(Role.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserGroupRoleAssignment> groupMono = userGroupRoleAssignmentService.insert(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }

      @Test
      @DisplayName("ユーザグループが存在しない場合はエラーになる")
      void cannotCreateUserGroupRoleAssignmentIfUserGroupDoesNotExist() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.save(any(UserGroupRoleAssignment.class)))
            .thenReturn(Mono.just(userGroupRoleAssignment));
        when(userGroupRoleAssignmentRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        when(userGroupRepository.findById(1L))
            .thenReturn(Mono.empty());
        when(roleRepository.findById(1L))
            .thenReturn(Mono.just(Role.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserGroupRoleAssignment> groupMono =
            userGroupRoleAssignmentService.insert(userGroupRoleAssignment);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("ロールが存在しない場合はエラーになる")
      void cannotCreateUserGroupRoleAssignmentIfRoleDoesNotExist() {
        // given
        UserGroupRoleAssignment userGroupRoleAssignment = UserGroupRoleAssignment.builder()
            .namespaceId(1L).userGroupId(1L).roleId(1L).createdBy(1L).build();
        when(userGroupRoleAssignmentRepository.save(any(UserGroupRoleAssignment.class)))
            .thenReturn(Mono.just(userGroupRoleAssignment));
        when(userGroupRoleAssignmentRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        when(userGroupRepository.findById(1L))
            .thenReturn(Mono.just(UserGroup.builder().id(1L).namespaceId(1L).build()));
        when(roleRepository.findById(1L))
            .thenReturn(Mono.empty());
        // when
        Mono<UserGroupRoleAssignment> groupMono =
            userGroupRoleAssignmentService.insert(userGroupRoleAssignment);
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
      @DisplayName("ターゲットグループをユニークキーで削除できる")
      void canDeleteByUniqueKeys() {
        // given
        when(userGroupRoleAssignmentRepository.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = userGroupRoleAssignmentService.deleteByUniqueKeys(1L, 1L, 1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}