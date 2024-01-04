package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.error.exception.UnauthorizedException;
import org.example.persistence.entity.Namespace;
import org.example.persistence.repository.NamespaceRepository;
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
class NamespaceServiceTest {

  @InjectMocks
  private NamespaceService namespaceService;
  @Mock
  private NamespaceRepository namespaceRepository;
  @Mock
  private SystemRoleService systemRoleService;

  @Nested
  class FindByUserId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを全件取得できる")
      void findAllTheIndexes() {
        // given
        Namespace namespace1 = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        Namespace namespace2 = Namespace.builder()
            .id(2L).name("namespace2").createdBy(2L).build();
        Namespace namespace3 = Namespace.builder()
            .id(3L).name("namespace3").createdBy(3L).build();
        when(namespaceRepository.findByUserId(1L))
            .thenReturn(Flux.just(namespace1, namespace2, namespace3));
        // when
        Flux<Namespace> namespaceFlux = namespaceService.findByUserId(1L);
        // then
        StepVerifier.create(namespaceFlux)
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(1L, "namespace1", 1L))
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(2L, "namespace2", 2L))
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(3L, "namespace3", 3L))
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
      @DisplayName("ネームスペースを登録できる")
      void insertTheIndex() {
        // given
        Namespace namespace1 = Namespace.builder().name("namespace1").createdBy(1L).build();
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(namespace1));
        when(namespaceRepository.findDuplicate("namespace1")).thenReturn(Mono.empty());
        // when
        Mono<Namespace> namespaceMono = namespaceService.insert(namespace1);
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(null, "namespace1", 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("登録済みと重複する場合はエラーになる")
      void cannotCreateDuplicateNamespace() {
        // given
        Namespace before = Namespace.builder().name("namespace1").createdBy(1L).build();
        Namespace after = Namespace.builder().name("namespace1").createdBy(2L).build();
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(namespaceRepository.findDuplicate("namespace1")).thenReturn(Mono.just(before));
        // when
        Mono<Namespace> namespaceMono = namespaceService.insert(after);
        // then
        StepVerifier.create(namespaceMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを更新できる")
      void updateTheIndex() {
        // given
        Namespace before = Namespace.builder()
            .id(1L).name("namespace2").createdBy(2L).build();
        Namespace after = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.just(before));
        when(namespaceRepository.findDuplicate("namespace1")).thenReturn(Mono.empty());
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.WRITE));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after, 1L);
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(1L, "namespace1", 1L))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("存在しないNamespaceの場合はエラーになる")
      void notExistingNamespaceCauseException() {
        // given
        Namespace after = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.empty());
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.WRITE));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after, 1L);
        // then
        StepVerifier.create(namespaceMono).expectError(NotExistingException.class).verify();
      }

      @Test
      @DisplayName("すでに登録済みの場合はエラーになる")
      void cannotUpdateWithDuplicate() {
        // given
        Namespace before = Namespace.builder()
            .id(2L).name("namespace2").createdBy(2L).build();
        Namespace after = Namespace.builder()
            .id(2L).name("namespace1").createdBy(2L).build();
        Namespace duplicate = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(2L)).thenReturn(Mono.just(before));
        when(namespaceRepository.findDuplicate("namespace1")).thenReturn(Mono.just(duplicate));
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(systemRoleService.aggregateSystemRolePermission(1L, 2L))
            .thenReturn(Mono.just(SystemRolePermission.WRITE));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after, 1L);
        // then
        StepVerifier.create(namespaceMono).expectError(RedundantException.class).verify();
      }

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void cannotUpdateWithoutPermission() {
        // given
        Namespace before = Namespace.builder()
            .id(1L).name("namespace2").createdBy(2L).build();
        Namespace after = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.just(before));
        when(namespaceRepository.findDuplicate("namespace1")).thenReturn(Mono.empty());
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.NONE));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after, 1L);
        // then
        StepVerifier.create(namespaceMono).expectError(UnauthorizedException.class).verify();
      }

      @Test
      @DisplayName("参照権限のみの場合はエラーになる")
      void cannotUpdateWithoutREADPermission() {
        // given
        Namespace before = Namespace.builder()
            .id(1L).name("namespace2").createdBy(2L).build();
        Namespace after = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.just(before));
        when(namespaceRepository.findDuplicate("namespace1")).thenReturn(Mono.empty());
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.READ));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after, 1L);
        // then
        StepVerifier.create(namespaceMono).expectError(UnauthorizedException.class).verify();
      }
    }
  }

  @Nested
  class Delete {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを削除できる")
      void deleteTheIndex() {
        // given
        when(namespaceRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.WRITE));
        // when
        Mono<Void> namespaceMono = namespaceService.deleteById(1L, 1L);
        // then
        StepVerifier.create(namespaceMono).verifyComplete();
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @Test
      @DisplayName("権限がない場合はエラーになる")
      void cannotDeleteWithoutPermission() {
        // given
        when(namespaceRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.NONE));
        // when
        Mono<Void> namespaceMono = namespaceService.deleteById(1L, 1L);
        // then
        StepVerifier.create(namespaceMono).expectError(UnauthorizedException.class).verify();
      }

      @Test
      @DisplayName("参照権限のみの場合はエラーになる")
      void cannotDeleteWithoutREADPermission() {
        // given
        when(namespaceRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(systemRoleService.aggregateSystemRolePermission(1L, 1L))
            .thenReturn(Mono.just(SystemRolePermission.READ));
        // when
        Mono<Void> namespaceMono = namespaceService.deleteById(1L, 1L);
        // then
        StepVerifier.create(namespaceMono).expectError(UnauthorizedException.class).verify();
      }
    }
  }
}