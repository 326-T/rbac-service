package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.SystemRole;
import org.example.persistence.entity.User;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.persistence.repository.SystemRoleRepository;
import org.example.persistence.repository.UserRepository;
import org.example.persistence.repository.UserSystemRolePermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class UserSystemRolePermissionServiceTest {

  @InjectMocks
  private UserSystemRolePermissionService userSystemRolePermissionService;
  @Mock
  private UserSystemRolePermissionRepository userSystemRolePermissionRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private SystemRoleRepository systemRoleRepository;

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザにシステムロール権限を付与できる")
      void insert() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .id(1L).namespaceId(1L).userId(1L).systemRoleId(1L).createdBy(1L).build();
        when(userSystemRolePermissionRepository.findDuplicate(1L, 1L))
            .thenReturn(Mono.empty());
        when(userSystemRolePermissionRepository.save(userSystemRolePermission))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(userRepository.findById(1L))
            .thenReturn(Mono.just(User.builder().id(1L).build()));
        when(systemRoleRepository.findById(1L))
            .thenReturn(Mono.just(SystemRole.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserSystemRolePermission> insertedUserSystemRolePermission = userSystemRolePermissionService.insert(userSystemRolePermission);
        // then
        StepVerifier.create(insertedUserSystemRolePermission)
            .assertNext(permission -> assertThat(permission)
                .extracting(UserSystemRolePermission::getId, UserSystemRolePermission::getNamespaceId,
                    UserSystemRolePermission::getUserId, UserSystemRolePermission::getSystemRoleId,
                    UserSystemRolePermission::getCreatedBy)
                .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicate() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .namespaceId(1L).userId(1L).systemRoleId(1L).build();
        SystemRole systemRole = SystemRole.builder().id(1L).namespaceId(1L).build();
        when(userSystemRolePermissionRepository.findDuplicate(1L, 1L))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(userSystemRolePermissionRepository.save(userSystemRolePermission))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(userRepository.findById(1L))
            .thenReturn(Mono.just(User.builder().id(1L).build()));
        when(systemRoleRepository.findById(1L))
            .thenReturn(Mono.just(SystemRole.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserSystemRolePermission> insertedUserSystemRolePermission = userSystemRolePermissionService.insert(userSystemRolePermission);
        // then
        StepVerifier.create(insertedUserSystemRolePermission)
            .expectError(RedundantException.class).verify();
      }

      @Test
      @DisplayName("ユーザが存在しない場合はエラーになる")
      void cannotCreateIfUserNotFound() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .namespaceId(1L).userId(1L).systemRoleId(1L).build();
        when(userSystemRolePermissionRepository.findDuplicate(1L, 1L))
            .thenReturn(Mono.empty());
        when(userSystemRolePermissionRepository.save(userSystemRolePermission))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(userRepository.findById(1L))
            .thenReturn(Mono.empty());
        when(systemRoleRepository.findById(1L))
            .thenReturn(Mono.just(SystemRole.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<UserSystemRolePermission> insertedUserSystemRolePermission = userSystemRolePermissionService.insert(userSystemRolePermission);
        // then
        StepVerifier.create(insertedUserSystemRolePermission)
            .expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("システムロールが存在しない場合はエラーになる")
      void cannotCreateIfSystemRoleNotFound() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .namespaceId(1L).userId(1L).systemRoleId(1L).build();
        when(userSystemRolePermissionRepository.findDuplicate(1L, 1L))
            .thenReturn(Mono.empty());
        when(userSystemRolePermissionRepository.save(userSystemRolePermission))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(userRepository.findById(1L))
            .thenReturn(Mono.just(User.builder().id(1L).build()));
        when(systemRoleRepository.findById(1L)).thenReturn(Mono.empty());
        // when
        Mono<UserSystemRolePermission> insertedUserSystemRolePermission = userSystemRolePermissionService.insert(userSystemRolePermission);
        // then
        StepVerifier.create(insertedUserSystemRolePermission)
            .expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("システムロールが異なるNamespaceIdの場合はエラーになる")
      void cannotCreateIfSystemRoleIsNotInNamespace() {
        // given
        UserSystemRolePermission userSystemRolePermission = UserSystemRolePermission.builder()
            .userId(1L).systemRoleId(1L).build();
        when(userSystemRolePermissionRepository.findDuplicate(1L, 1L))
            .thenReturn(Mono.empty());
        when(userSystemRolePermissionRepository.save(userSystemRolePermission))
            .thenReturn(Mono.just(userSystemRolePermission));
        when(userRepository.findById(1L))
            .thenReturn(Mono.just(User.builder().id(1L).build()));
        when(systemRoleRepository.findById(1L))
            .thenReturn(Mono.just(SystemRole.builder().id(1L).namespaceId(2L).build()));
        // when
        Mono<UserSystemRolePermission> insertedUserSystemRolePermission = userSystemRolePermissionService.insert(userSystemRolePermission);
        // then
        StepVerifier.create(insertedUserSystemRolePermission)
            .expectError(NotExistingException.class).verify();
      }
    }
  }

  @Nested
  class DeleteByUniqueKeys {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザに付与されたシステムロール権限を削除できる")
      void deleteByUniqueKeys() {
        // given
        when(userSystemRolePermissionRepository.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<Void> deleted = userSystemRolePermissionService.deleteByUniqueKeys(1L, 1L, 1L);
        // then
        StepVerifier.create(deleted).verifyComplete();
      }
    }
  }
}