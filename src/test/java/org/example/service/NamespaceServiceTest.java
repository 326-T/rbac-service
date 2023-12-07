package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Namespace;
import org.example.persistence.repository.NamespaceRepository;
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

  @Nested
  class Count {

    @Nested
    class regular {

      @Test
      @DisplayName("ネームスペースの件数を取得できる")
      void countTheIndexes() {
        // given
        when(namespaceRepository.count()).thenReturn(Mono.just(3L));
        // when
        Mono<Long> count = namespaceService.count();
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
      @DisplayName("ネームスペースを全件取得できる")
      void findAllTheIndexes() {
        // given
        Namespace namespace1 = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        Namespace namespace2 = Namespace.builder()
            .id(2L).name("namespace2").createdBy(2L).build();
        Namespace namespace3 = Namespace.builder()
            .id(3L).name("namespace3").createdBy(3L).build();
        when(namespaceRepository.findAll()).thenReturn(Flux.just(namespace1, namespace2, namespace3));
        // when
        Flux<Namespace> namespaceFlux = namespaceService.findAll();
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
  class FindById {

    @Nested
    class regular {

      @Test
      @DisplayName("ネームスペースをIDで取得できる")
      void findByIdTheIndex() {
        // given
        Namespace namespace1 = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.just(namespace1));
        // when
        Mono<Namespace> namespaceMono = namespaceService.findById(1L);
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(namespace -> assertThat(namespace)
                .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                .containsExactly(1L, "namespace1", 1L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class insert {

    @Nested
    class regular {

      @Test
      @DisplayName("ネームスペースを登録できる")
      void insertTheIndex() {
        // given
        Namespace namespace1 = Namespace.builder().name("namespace1").createdBy(1L).build();
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(namespace1));
        when(namespaceRepository.findDuplicated("namespace1")).thenReturn(Mono.empty());
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
    class irregular {

      @Test
      @DisplayName("登録済みと重複する場合はエラーになる")
      void cannotCreateDuplicatedNamespace() {
        // given
        Namespace before = Namespace.builder().name("namespace1").createdBy(1L).build();
        Namespace after = Namespace.builder().name("namespace1").createdBy(2L).build();
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        when(namespaceRepository.findDuplicated("namespace1")).thenReturn(Mono.just(before));
        // when
        Mono<Namespace> namespaceMono = namespaceService.insert(after);
        // then
        StepVerifier.create(namespaceMono).expectError(RedundantException.class).verify();
      }
    }
  }

  @Nested
  class update {

    @Nested
    class regular {

      @Test
      @DisplayName("ネームスペースを更新できる")
      void updateTheIndex() {
        // given
        Namespace before = Namespace.builder()
            .id(2L).name("namespace2").createdBy(2L).build();
        Namespace after = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.just(before));
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after);
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
    class irregular {

      @Test
      @DisplayName("存在しないNamespaceの場合はエラーになる")
      void notExistingNamespaceCauseException() {
        // given
        Namespace after = Namespace.builder()
            .id(1L).name("namespace1").createdBy(1L).build();
        when(namespaceRepository.findById(1L)).thenReturn(Mono.empty());
        when(namespaceRepository.save(any(Namespace.class))).thenReturn(Mono.just(after));
        // when
        Mono<Namespace> namespaceMono = namespaceService.update(after);
        // then
        StepVerifier.create(namespaceMono).expectError(NotExistingException.class).verify();
      }
    }
  }

  @Nested
  class delete {

    @Nested
    class regular {

      @Test
      @DisplayName("ネームスペースを削除できる")
      void deleteTheIndex() {
        // given
        when(namespaceRepository.deleteById(1L)).thenReturn(Mono.empty());
        // when
        Mono<Void> namespaceMono = namespaceService.deleteById(1L);
        // then
        StepVerifier.create(namespaceMono).verifyComplete();
      }
    }
  }
}