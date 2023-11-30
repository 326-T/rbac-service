package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ロールの件数を取得できる")
      void countTheIndexes() {
        // given
        when(roleRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = roleService.count();
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
      @DisplayName("ロールを全件取得できる")
      void findAllTheIndexes() {
        // given
        Role role1 = Role.builder()
            .id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        Role role2 = Role.builder()
            .id(2L).namespaceId(2L).name("operator").createdBy(2L).build();
        Role role3 = Role.builder()
            .id(3L).namespaceId(3L).name("security").createdBy(3L).build();
        when(roleRepository.findAll()).thenReturn(Flux.just(role1, role2,
            role3));
        // when
        Flux<Role> groupFlux = roleService.findAll();
        // then
        StepVerifier.create(groupFlux)
            .assertNext(
                group -> assertThat(group)
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developer", 1L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(2L, 2L, "operator", 2L))
            .assertNext(
                group -> assertThat(group)
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(3L, 3L, "security", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("ロールをIDで取得できる")
      void findByIdTheIndex() {
        // given
        Role role1 = Role.builder()
            .id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role1));
        // when
        Mono<Role> groupMono = roleService.findById(1L);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developer", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("ロールを登録できる")
      void insertTheIndex() {
        // given
        Role role1 = Role.builder()
            .namespaceId(1L).name("developer").createdBy(1L).build();
        when(roleRepository.save(any(Role.class))).thenReturn(
            Mono.just(role1));
        // when
        Mono<Role> groupMono = roleService.insert(role1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(null, 1L, "developer", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("ロールを更新できる")
      void updateTheIndex() {
        // given
        Role role1 = Role.builder()
            .id(1L).namespaceId(1L).name("developer").createdBy(1L).build();
        when(roleRepository.findById(1L)).thenReturn(Mono.just(role1));
        when(roleRepository.save(any(Role.class))).thenReturn(
            Mono.just(role1));
        // when
        Mono<Role> groupMono = roleService.update(role1);
        // then
        StepVerifier.create(groupMono)
            .assertNext(
                group -> assertThat(group)
                    .extracting(Role::getId, Role::getNamespaceId,
                        Role::getName, Role::getCreatedBy)
                    .containsExactly(1L, 1L, "developer", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("ロールを削除できる")
      void deleteTheIndex() {
        // given
        when(roleRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> groupMono = roleService.deleteById(1L);
        // then
        StepVerifier.create(groupMono).verifyComplete();
      }
    }
  }
}