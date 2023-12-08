package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Target;
import org.example.persistence.entity.User;
import org.example.service.ReactiveContextService;
import org.example.service.TargetService;
import org.example.web.filter.AuthenticationWebFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = TargetRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class TargetRestControllerTest {

  @MockBean
  private TargetService targetService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットの件数を取得できる")
      void countTheIndexes() {
        // given
        when(targetService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/targets/count")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Nested
  class findAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットを全件取得できる")
      void findAllTheIndexes() {
        // given
        Target target1 = Target.builder()
            .id(1L).namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        Target target2 = Target.builder()
            .id(2L).namespaceId(2L).objectIdRegex("object-id-2").createdBy(2L).build();
        Target target3 = Target.builder()
            .id(3L).namespaceId(3L).objectIdRegex("object-id-3").createdBy(3L).build();
        when(targetService.findAll()).thenReturn(Flux.just(target1, target2, target3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/targets")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Target.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId,
                        Target::getObjectIdRegex, Target::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, "object-id-1", 1L),
                        tuple(2L, 2L, "object-id-2", 2L),
                        tuple(3L, 3L, "object-id-3", 3L)
                    )
            );
      }
    }
  }

  @Nested
  class findById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットをIDで取得できる")
      void canGetTheTargetById() {
        // given
        Target target = Target.builder()
            .id(1L).namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        when(targetService.findById(1L)).thenReturn(Mono.just(target));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/targets/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId,
                        Target::getObjectIdRegex, Target::getCreatedBy)
                    .containsExactly(1L, 1L, "object-id-1", 1L));
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットを更新できる")
      void canUpdateTheTarget() {
        // given
        Target target = Target.builder()
            .id(2L).namespaceId(1L).objectIdRegex("OBJECT-ID-2").createdBy(1L).build();
        when(targetService.update(any(Target.class))).thenReturn(Mono.just(target));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/targets/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "objectIdRegex": "OBJECT-ID-2"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(2L, 1L, "OBJECT-ID-2", 1L));
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットを登録できる")
      void canSaveTheTarget() {
        // given
        Target target = Target.builder()
            .id(4L).namespaceId(1L).objectIdRegex("object-id-4").createdBy(1L).build();
        when(targetService.insert(any(Target.class))).thenReturn(Mono.just(target));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/targets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "objectIdRegex": "object-id-4"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Target.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Target::getId, Target::getNamespaceId, Target::getObjectIdRegex,
                        Target::getCreatedBy)
                    .containsExactly(4L, 1L, "object-id-4", 1L));
      }
    }
  }

  @Nested
  class deleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットを削除できる")
      void canDeleteTheTargetById() {
        // given
        when(targetService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/targets/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}