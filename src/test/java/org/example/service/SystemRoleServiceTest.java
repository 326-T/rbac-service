package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Namespace;
import org.example.persistence.entity.SystemRole;
import org.example.persistence.entity.UserSystemRolePermission;
import org.example.persistence.repository.SystemRoleRepository;
import org.example.persistence.repository.UserSystemRolePermissionRepository;
import org.example.util.constant.SystemRolePermission;
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
class SystemRoleServiceTest {

  @InjectMocks
  private SystemRoleService systemRoleService;
  @Mock
  private SystemRoleRepository systemRoleRepository;
  @Mock
  private UserSystemRolePermissionRepository userSystemRolePermissionRepository;

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースIDでシステムロールを取得できる")
      void findByNamespaceId() {
        // given
        SystemRole developRead = SystemRole.builder()
            .id(1L)
            .name("develop_READ")
            .namespaceId(1L)
            .permission("READ")
            .build();
        SystemRole developWrite = SystemRole.builder()
            .id(2L)
            .name("develop_WRITE")
            .namespaceId(1L)
            .permission("WRITE")
            .build();
        when(systemRoleRepository.findByNamespaceId(1L))
            .thenReturn(Flux.just(developRead, developWrite));
        // when
        Flux<SystemRole> systemRoleFlux = systemRoleRepository.findByNamespaceId(1L);
        // then
        StepVerifier.create(systemRoleFlux)
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        1L, "develop_READ",
                        1L, "READ")
            )
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        2L, "develop_WRITE",
                        1L, "WRITE")
            )
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindByUserId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーIDに紐づくシステムロールを取得できる")
      void findByUserId() {
        // given
        SystemRole developRead = SystemRole.builder()
            .id(1L)
            .name("develop_READ")
            .namespaceId(1L)
            .permission("READ")
            .build();
        SystemRole developWrite = SystemRole.builder()
            .id(2L)
            .name("develop_WRITE")
            .namespaceId(1L)
            .permission("WRITE")
            .build();
        when(systemRoleRepository.findByUserIdAndNamespaceId(1L, 1L))
            .thenReturn(Flux.just(developRead, developWrite));
        // when
        Flux<SystemRole> systemRoleFlux = systemRoleRepository.findByUserIdAndNamespaceId(1L, 1L);
        // then
        StepVerifier.create(systemRoleFlux)
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        1L, "develop_READ",
                        1L, "READ")
            )
            .assertNext(systemRole ->
                assertThat(systemRole)
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        2L, "develop_WRITE",
                        1L, "WRITE")
            )
            .verifyComplete();
      }
    }
  }

  @Nested
  class CreateSystemRole {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("システムロールを新規登録できる")
      void createSystemRole() {
        // given
        Namespace namespace = Namespace.builder()
            .id(1L).name("staging").createdBy(1L).build();
        SystemRole readRole = SystemRole.builder()
            .name("staging_参照権限")
            .namespaceId(2L)
            .permission("READ")
            .build();
        when(systemRoleRepository.save(any(SystemRole.class)))
            .thenReturn(Mono.just(readRole));
        when(userSystemRolePermissionRepository.save(any(UserSystemRolePermission.class)))
            .thenReturn(Mono.just(UserSystemRolePermission.builder().build()));
        // when
        Mono<Void> result = systemRoleService.createSystemRole(namespace, 1L);
        // then
        StepVerifier.create(result).verifyComplete();
      }
    }
  }

  @Nested
  class AggregateSystemRolePermission {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ユーザーIDとネームスペースIDに紐づくシステムロールの権限を取得できる")
      void aggregateSystemRolePermission() {
        // given
        SystemRole developRead = SystemRole.builder()
            .id(1L)
            .name("develop_READ")
            .namespaceId(1L)
            .permission("READ")
            .build();
        SystemRole developWrite = SystemRole.builder()
            .id(2L)
            .name("develop_WRITE")
            .namespaceId(1L)
            .permission("WRITE")
            .build();
        when(systemRoleRepository.findByUserIdAndNamespaceId(1L, 1L))
            .thenReturn(Flux.just(developRead, developWrite));
        // when
        Mono<SystemRolePermission> result = systemRoleService.aggregateSystemRolePermission(1L, 1L);
        // then
        StepVerifier.create(result)
            .assertNext(permission -> assertThat(permission).isEqualTo(SystemRolePermission.WRITE))
            .verifyComplete();
      }

      @Test
      @DisplayName("READ権限のみの場合はREADを返す")
      void returnReadIfOnlyReadPermission() {
        // given
        SystemRole developRead = SystemRole.builder()
            .id(1L)
            .name("develop_READ")
            .namespaceId(1L)
            .permission("READ")
            .build();
        SystemRole stagingWrite = SystemRole.builder()
            .id(2L)
            .name("staging_READ")
            .namespaceId(1L)
            .permission("READ")
            .build();
        when(systemRoleRepository.findByUserIdAndNamespaceId(1L, 1L))
            .thenReturn(Flux.just(developRead, stagingWrite));
        // when
        Mono<SystemRolePermission> result = systemRoleService.aggregateSystemRolePermission(1L, 1L);
        // then
        StepVerifier.create(result)
            .assertNext(permission -> assertThat(permission).isEqualTo(SystemRolePermission.READ))
            .verifyComplete();
      }

      @Test
      @DisplayName("権限がない場合はNONEを返す")
      void returnNoneIfNoPermission() {
        // given
        when(systemRoleRepository.findByUserIdAndNamespaceId(1L, 1L))
            .thenReturn(Flux.empty());
        // when
        Mono<SystemRolePermission> result = systemRoleService.aggregateSystemRolePermission(1L, 1L);
        // then
        StepVerifier.create(result)
            .assertNext(permission -> assertThat(permission).isEqualTo(SystemRolePermission.NONE))
            .verifyComplete();
      }
    }
  }
}