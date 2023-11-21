package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Path;
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
class PathRepositoryTest {

  @Autowired
  private PathRepository pathRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("パスの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = pathRepository.count();
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
      @DisplayName("パスを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Path> pathFlux = pathRepository.findAll();
        // then
        StepVerifier.create(pathFlux).assertNext(
                path -> assertThat(path).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(1L, 1L, "/user-service/v1/", 1L)).assertNext(
                path -> assertThat(path).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(2L, 2L, "/billing-service/v1/", 2L)).assertNext(
                path -> assertThat(path).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(3L, 3L, "/inventory-service/v2/", 3L))
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
      @DisplayName("パスをIDで取得できる")
      void findUserById() {
        // when
        Mono<Path> pathMono = pathRepository.findById(1L);
        // then
        StepVerifier.create(pathMono).assertNext(
                path -> assertThat(path).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(1L, 1L, "/user-service/v1/", 1L))
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
      @DisplayName("パスを更新できる")
      void updatePath() {
        // given
        Path path = Path.builder().id(2L).serviceId(1L).regex("/replace-service/v1/").createdBy(1L)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        // when
        Mono<Path> pathMono = pathRepository.save(path);
        // then
        StepVerifier.create(pathMono).assertNext(
                path1 -> assertThat(path1).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(2L, 1L, "/replace-service/v1/", 1L))
            .verifyComplete();
        pathRepository.findById(2L).as(StepVerifier::create).assertNext(
                path1 -> assertThat(path1).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(2L, 1L, "/replace-service/v1/", 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("パスを新規登録できる")
      void insertPath() {
        // given
        Path path = Path.builder().serviceId(1L).regex("/next-service/v1/").createdBy(1L).build();
        // when
        Mono<Path> pathMono = pathRepository.save(path);
        // then
        StepVerifier.create(pathMono).assertNext(
                path1 -> assertThat(path1).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(4L, 1L, "/next-service/v1/", 1L))
            .verifyComplete();
        pathRepository.findById(4L).as(StepVerifier::create).assertNext(
                path1 -> assertThat(path1).extracting(Path::getId, Path::getServiceId, Path::getRegex,
                    Path::getCreatedBy).containsExactly(4L, 1L, "/next-service/v1/", 1L))
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
      @DisplayName("パスをIDで削除できる")
      void deletePathById() {
        // when
        Mono<Void> voidMono = pathRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        pathRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}