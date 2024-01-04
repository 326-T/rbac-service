package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.entity.Role;
import org.example.persistence.entity.RoleEndpointPermission;
import org.example.persistence.repository.EndpointRepository;
import org.example.persistence.repository.RoleEndpointPermissionRepository;
import org.example.persistence.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class RoleEndpointPermissionServiceTest {

  @InjectMocks
  private RoleEndpointPermissionService roleEndpointPermissionService;
  @Mock
  private RoleEndpointPermissionRepository roleEndpointPermissionRepository;
  @Mock
  private RoleRepository roleRepository;
  @Mock
  private EndpointRepository endpointRepository;

  @Nested
  class Insert {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループを登録できる")
      void insertTheIndex() {
        // given
        RoleEndpointPermission roleEndpointPermission1 = RoleEndpointPermission.builder()
            .namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionRepository.save(any(RoleEndpointPermission.class)))
            .thenReturn(Mono.just(roleEndpointPermission1));
        when(roleEndpointPermissionRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        when(roleRepository.findById(1L))
            .thenReturn(Mono.just(Role.builder().id(1L).namespaceId(1L).build()));
        when(endpointRepository.findById(1L))
            .thenReturn(Mono.just(Endpoint.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<RoleEndpointPermission> groupMono = roleEndpointPermissionService.insert(
            roleEndpointPermission1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(group -> assertThat(group)
                .extracting(RoleEndpointPermission::getId,
                    RoleEndpointPermission::getNamespaceId,
                    RoleEndpointPermission::getRoleId,
                    RoleEndpointPermission::getEndpointId,
                    RoleEndpointPermission::getCreatedBy)
                .containsExactly(null, 1L, 1L, 1L, 1L))
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
        RoleEndpointPermission before = RoleEndpointPermission.builder()
            .namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        RoleEndpointPermission after = RoleEndpointPermission.builder()
            .namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionRepository.save(any(RoleEndpointPermission.class)))
            .thenReturn(Mono.just(after));
        when(roleEndpointPermissionRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.just(before));
        when(roleRepository.findById(1L))
            .thenReturn(Mono.just(Role.builder().id(1L).namespaceId(1L).build()));
        when(endpointRepository.findById(1L))
            .thenReturn(Mono.just(Endpoint.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<RoleEndpointPermission> groupMono = roleEndpointPermissionService.insert(after);
        // then
        StepVerifier.create(groupMono).expectError(RedundantException.class).verify();
      }

      @Test
      @DisplayName("存在しないRoleの場合はエラーになる")
      void cannotCreateEndpointWithNonExistingRole() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionRepository.save(any(RoleEndpointPermission.class)))
            .thenReturn(Mono.just(roleEndpointPermission));
        when(roleEndpointPermissionRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        when(roleRepository.findById(1L))
            .thenReturn(Mono.empty());
        when(endpointRepository.findById(1L))
            .thenReturn(Mono.just(Endpoint.builder().id(1L).namespaceId(1L).build()));
        // when
        Mono<RoleEndpointPermission> groupMono = roleEndpointPermissionService.insert(roleEndpointPermission);
        // then
        StepVerifier.create(groupMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("存在しないEndpointの場合はエラーになる")
      void cannotCreateEndpointWithNonExistingEndpoint() {
        // given
        RoleEndpointPermission roleEndpointPermission = RoleEndpointPermission.builder()
            .namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionRepository.save(any(RoleEndpointPermission.class)))
            .thenReturn(Mono.just(roleEndpointPermission));
        when(roleEndpointPermissionRepository.findDuplicate(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        when(roleRepository.findById(1L))
            .thenReturn(Mono.just(Role.builder().id(1L).namespaceId(1L).build()));
        when(endpointRepository.findById(1L))
            .thenReturn(Mono.empty());
        // when
        Mono<RoleEndpointPermission> groupMono = roleEndpointPermissionService.insert(roleEndpointPermission);
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
      @DisplayName("ターゲットグループを削除できる")
      void canDeleteByUniqueKeys() {
        // given
        when(roleEndpointPermissionRepository.deleteByUniqueKeys(1L, 1L, 1L))
            .thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = roleEndpointPermissionService.deleteByUniqueKeys(1L, 1L, 1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}