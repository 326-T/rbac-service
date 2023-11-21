package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.example.listener.FlywayTestExecutionListener;
import org.example.persistence.entity.Service;
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
class ServiceRepositoryTest {

  @Autowired
  private ServiceRepository serviceRepository;

  @Order(1)
  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("サービスの件数を取得できる")
      void countTheIndexes() {
        // when
        Mono<Long> count = serviceRepository.count();
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
      @DisplayName("サービスを全件取得できる")
      void findAllTheIndexes() {
        // when
        Flux<Service> serviceFlux = serviceRepository.findAll();
        // then
        StepVerifier.create(serviceFlux)
            .assertNext(
                service -> assertThat(service)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(1L, "front", 1L))
            .assertNext(
                service -> assertThat(service)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(2L, "backend", 2L))
            .assertNext(
                service -> assertThat(service)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(3L, "database", 3L))
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
      @DisplayName("サービスをIDで取得できる")
      void findUserById() {
        // when
        Mono<Service> serviceMono = serviceRepository.findById(1L);
        // then
        StepVerifier.create(serviceMono)
            .assertNext(
                service -> assertThat(service)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(1L, "front", 1L))
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
      @DisplayName("サービスを更新できる")
      void updateService() {
        // given
        Service service = Service.builder()
            .id(2L)
            .name("BACKEND")
            .createdBy(1L)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        // when
        Mono<Service> serviceMono = serviceRepository.save(service);
        // then
        StepVerifier.create(serviceMono)
            .assertNext(
                service1 -> assertThat(service1)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(2L, "BACKEND", 1L))
            .verifyComplete();
        serviceRepository.findById(2L).as(StepVerifier::create)
            .assertNext(
                service1 -> assertThat(service1)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(2L, "BACKEND", 1L))
            .verifyComplete();
      }

      @Test
      @DisplayName("サービスを新規登録できる")
      void insertService() {
        // given
        Service service = Service.builder()
            .name("auth")
            .createdBy(1L)
            .build();
        // when
        Mono<Service> serviceMono = serviceRepository.save(service);
        // then
        StepVerifier.create(serviceMono)
            .assertNext(
                service1 -> assertThat(service1)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(4L, "auth", 1L))
            .verifyComplete();
        serviceRepository.findById(4L).as(StepVerifier::create)
            .assertNext(
                service1 -> assertThat(service1)
                    .extracting(Service::getId, Service::getName, Service::getCreatedBy)
                    .containsExactly(4L, "auth", 1L))
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
      @DisplayName("サービスをIDで削除できる")
      void deleteServiceById() {
        // when
        Mono<Void> voidMono = serviceRepository.deleteById(3L);
        // then
        StepVerifier.create(voidMono).verifyComplete();
        serviceRepository.findById(3L).as(StepVerifier::create).verifyComplete();
      }
    }
  }
}