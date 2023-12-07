package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Namespace;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class NamespaceRepositoryTest {

  @Autowired
  private NamespaceRepository namespaceRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ネームスペースの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = namespaceRepository.count();
        // then
        StepVerifier.create(count).expectNext(3L).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ネームスペースを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Namespace> namespaceFlux = namespaceRepository.findAll();
        // then
        StepVerifier.create(namespaceFlux)
            .assertNext(
                namespace -> assertThat(namespace)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(1L, "develop", 1L))
            .assertNext(
                namespace -> assertThat(namespace)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(2L, "staging", 2L))
            .assertNext(
                namespace -> assertThat(namespace)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(3L, "production", 3L))
            .verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ネームスペースをIDで取得できる")
      void findUserById() {
        // when
        Mono<Namespace> namespaceMono = namespaceRepository.findById(1L);
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(
                namespace -> assertThat(namespace)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(1L, "develop", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class Save {

    @Nested
    class regular {

      @Test
      @DisplayName("ネームスペースを更新できる")
      void updateNamespace() {
        // given
        Namespace namespace = Namespace.builder()
            .id(2L)
            .name("STAGING")
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Namespace> namespaceMono = namespaceRepository.save(namespace);
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(
                namespace1 -> assertThat(namespace1)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(2L, "STAGING", 1L))
            .verifyComplete();
        namespaceRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                namespace1 -> assertThat(namespace1)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(2L, "STAGING", 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("ネームスペースを新規登録できる")
      void insertNamespace() {
        // given
        Namespace namespace = Namespace.builder()
            .name("integration")
            .createdBy(1L)
            .build();
        // when
        Mono<Namespace> namespaceMono = namespaceRepository.save(namespace);
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(
                namespace1 -> assertThat(namespace1)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(4L, "integration", 1L))
            .verifyComplete();
        namespaceRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                namespace1 -> assertThat(namespace1)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(4L, "integration", 1L))
            .verifyComplete();
      }
    }
  }

  @Order(2)
  @Nested
  @TestExecutionListeners(listeners = {
      FlywayTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ネームスペースをIDで削除できる")
      void deleteNamespaceById() {
        // when
        Mono<Void> voidMono = namespaceRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        namespaceRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }

  @Order(1)
  @Nested
  class FindDuplicated {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("重複するネームスペースを検索できる")
      void findDuplicated() {
        // when
        Mono<Namespace> namespaceMono = namespaceRepository.findDuplicated("develop");
        // then
        StepVerifier.create(namespaceMono)
            .assertNext(
                namespace -> assertThat(namespace)
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(1L, "develop", 1L))
            .verifyComplete();
      }
    }
  }
}