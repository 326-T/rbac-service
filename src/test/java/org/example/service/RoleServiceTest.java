package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Role;
import org.example.persistence.repository.RoleRepository;
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
class RoleServiceTest {

  @InjectMocks
  private RoleService roleService;
  @Mock
  private RoleRepository roleRepository;

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールを名前空間IDで取得できる")
      void findAllTheIndexes() {
        // given
        Role role1 = Role.builder().id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        Role role2 = Role.builder().id(2L).namespaceId(1L).name("operator").createdBy(2L).build();
        Role role3 = Role.builder().id(3L).namespaceId(1L).name("security").createdBy(3L).build();
        when(roleRepository.findByNamespaceId(1L)).thenReturn(Flux.just(role1, role2, role3));
        // when
        Flux<Role> groupFlux = roleService.findByNamespaceId(1L);
        // then
        StepVerifier.create(groupFlux)
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(1L, 1L, "developer", 1L))
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(2L, 1L, "operator", 2L))
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(3L, 1L, "security", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindByNamespaceIdAndUserGroupId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールを名前空間IDで取得できる")
      void findAllTheIndexes() {
        // given
        Role role1 = Role.builder().id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        Role role2 = Role.builder().id(2L).namespaceId(1L).name("operator").createdBy(2L).build();
        Role role3 = Role.builder().id(3L).namespaceId(1L).name("security").createdBy(3L).build();
        when(roleRepository.findByNamespaceIdAndUserGroupId(1L, 1L))
            .thenReturn(Flux.just(role1, role2, role3));
        // when
        Flux<Role> groupFlux = roleService.findByNamespaceIdAndUserGroupId(1L, 1L);
        // then
        StepVerifier.create(groupFlux)
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(1L, 1L, "developer", 1L))
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(2L, 1L, "operator", 2L))
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(3L, 1L, "security", 3L))
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
      @DisplayName("ロールを登録できる")
      void insertTheIndex() {
        // given
        Role role1 = Role.builder().namespaceId(1L).name("developer").createdBy(1L).build();
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(role1));
        when(roleRepository.findDuplicate(1L, "developer")).thenReturn(Mono.empty());
        // when
        Mono<Role> groupMono = roleService.insert(role1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId, Role::getName, Role::getCreatedBy)
                .containsExactly(null, 1L, "developer", 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotCreateDuplicateEndpoint() {
        // given
        Role before = Role.builder().id(2L).namespaceId(2L).name("operator").createdBy(2L).build();
        Role after = Role.builder().id(2L).namespaceId(2L).name("operator").createdBy(2L).build();
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(before));
        when(roleRepository.findDuplicate(2L, "operator")).thenReturn(Mono.just(before));
        // when
        Mono<Role> groupMono = roleService.insert(after);
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
      @DisplayName("ロールを更新できる")
      void updateTheIndex() {
        // given
        Role before = Role.builder().id(2L).namespaceId(2L).name("operator").createdBy(2L).build();
        Role after = Role.builder().id(2L).namespaceId(2L).name("developer").createdBy(2L).build();
        when(roleRepository.findById(2L)).thenReturn(Mono.just(before));
        when(roleRepository.findDuplicate(2L, "developer")).thenReturn(Mono.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(after));
        // when
        Mono<Role> groupMono = roleService.update(after);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(Role::getId, Role::getNamespaceId,
                    Role::getName, Role::getCreatedBy)
                .containsExactly(2L, 2L, "developer", 2L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないロールの場合はエラーになる")
      void notExistingRoleCauseException() {
        // given
        Role after = Role.builder().id(2L).namespaceId(2L).name("developer").createdBy(2L).build();
        when(roleRepository.findById(2L)).thenReturn(Mono.empty());
        when(roleRepository.findDuplicate(2L, "developer")).thenReturn(Mono.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(after));
        // when
        Mono<Role> groupMono = roleService.update(after);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotUpdateWithDifferentNamespaceId() {
        // given
        Role before = Role.builder().id(2L).namespaceId(2L).name("operator").createdBy(2L).build();
        Role after = Role.builder().id(2L).namespaceId(999L).name("developer").createdBy(2L).build();
        when(roleRepository.findById(2L)).thenReturn(Mono.just(before));
        when(roleRepository.findDuplicate(2L, "developer")).thenReturn(Mono.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(after));
        // when
        Mono<Role> groupMono = roleService.update(after);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // given
        Role before = Role.builder().id(2L).namespaceId(2L).name("operator").createdBy(2L).build();
        Role after = Role.builder().id(2L).namespaceId(2L).name("developer").createdBy(2L).build();
        Role duplicate = Role.builder().id(3L).namespaceId(2L).name("developer").createdBy(2L).build();
        when(roleRepository.findById(2L)).thenReturn(Mono.just(before));
        when(roleRepository.findDuplicate(2L, "developer")).thenReturn(Mono.just(duplicate));
        when(roleRepository.save(any(Role.class))).thenReturn(Mono.just(after));
        // when
        Mono<Role> groupMono = roleService.update(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class Delete {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ロールを削除できる")
      void deleteTheIndex() {
        // given
        Role role = Role.builder().id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));
        when(roleRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = roleService.deleteById(1L, 1L);
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
        when(roleRepository.findById(1L)).thenReturn(Mono.empty());
        when(roleRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = roleService.deleteById(1L, 1L);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("namespaceIdが一致しない場合はエラーになる")
      void cannotDeleteWithDifferentNamespaceId() {
        // given
        Role role = Role.builder().id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role));
        when(roleRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = roleService.deleteById(1L, 999L);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }
    }
  }
}