package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.RoleEndpointPermission;
import org.example.persistence.repository.RoleEndpointPermissionRepository;
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
class RoleEndpointPermissionServiceTest {

  @InjectMocks
  private RoleEndpointPermissionService roleEndpointPermissionService;
  @Mock
  private RoleEndpointPermissionRepository roleEndpointPermissionRepository;

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(roleEndpointPermissionRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = roleEndpointPermissionService.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Nested
  class FindAll {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        RoleEndpointPermission roleEndpointPermission1 = RoleEndpointPermission.builder()
            .id(1L).namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        RoleEndpointPermission roleEndpointPermission2 = RoleEndpointPermission.builder()
            .id(2L).namespaceId(2L).roleId(2L).endpointId(2L).createdBy(2L).build();
        RoleEndpointPermission roleEndpointPermission3 = RoleEndpointPermission.builder()
            .id(3L).namespaceId(3L).roleId(3L).endpointId(3L).createdBy(3L).build();
        when(roleEndpointPermissionRepository.findAll()).thenReturn(
            Flux.just(roleEndpointPermission1, roleEndpointPermission2,
                roleEndpointPermission3));
        // when
        Flux<RoleEndpointPermission> groupFlux = roleEndpointPermissionService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(
                group -> assertThat(group)
                    .extracting(RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, 2L, 2L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(3L, 3L, 3L, 3L, 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットグループをIDで取得できる")
      void findByIdTheIndex() {
        // given
        RoleEndpointPermission roleEndpointPermission1 = RoleEndpointPermission.builder()
            .id(1L).namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionRepository.findById(1L)).thenReturn(
            Mono.just(roleEndpointPermission1));
        // when
        Mono<RoleEndpointPermission> groupMono = roleEndpointPermissionService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットグループを登録できる")
      void insertTheIndex() {
        // given
        RoleEndpointPermission roleEndpointPermission1 = RoleEndpointPermission.builder()
            .namespaceId(1L).roleId(1L).endpointId(1L).createdBy(1L).build();
        when(roleEndpointPermissionRepository.save(any(RoleEndpointPermission.class))).thenReturn(
            Mono.just(roleEndpointPermission1));
        // when
        Mono<RoleEndpointPermission> groupMono = roleEndpointPermissionService.insert(
            roleEndpointPermission1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(RoleEndpointPermission::getId,
                        RoleEndpointPermission::getNamespaceId,
                        RoleEndpointPermission::getRoleId,
                        RoleEndpointPermission::getEndpointId,
                        RoleEndpointPermission::getCreatedBy)
                    .containsExactly(null, 1L, 1L, 1L, 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("ターゲットグループを削除できる")
      void deleteTheIndex() {
        // given
        when(roleEndpointPermissionRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = roleEndpointPermissionService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}