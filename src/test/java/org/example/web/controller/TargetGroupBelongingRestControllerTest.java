package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.TargetGroupBelonging;
import org.example.persistence.entity.User;
import org.example.service.ReactiveContextService;
import org.example.service.TargetGroupBelongingService;
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
    controllers = TargetGroupBelongingRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class TargetGroupBelongingRestControllerTest {

  @MockBean
  private TargetGroupBelongingService targetGroupBelongingService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係の件数を取得できる")
      void countTheIndexes() {
        // given
        when(targetGroupBelongingService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings/count")
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
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係を全件取得できる")
      void findAllTheIndexes() {
        // given
        TargetGroupBelonging targetGroupBelonging1 = TargetGroupBelonging.builder()
            .id(1L).namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        TargetGroupBelonging targetGroupBelonging2 = TargetGroupBelonging.builder()
            .id(2L).namespaceId(2L).targetId(2L).targetGroupId(2L).createdBy(2L).build();
        TargetGroupBelonging targetGroupBelonging3 = TargetGroupBelonging.builder()
            .id(3L).namespaceId(3L).targetId(3L).targetGroupId(3L).createdBy(3L).build();
        when(targetGroupBelongingService.findAll()).thenReturn(
            Flux.just(targetGroupBelonging1, targetGroupBelonging2, targetGroupBelonging3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(TargetGroupBelonging.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId, TargetGroupBelonging::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, 1L, 1L, 1L),
                        tuple(2L, 2L, 2L, 2L, 2L),
                        tuple(3L, 3L, 3L, 3L, 3L)
                    )
            );
      }
    }
  }

  @Nested
  class findById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係をIDで取得できる")
      void canGetTheTargetGroupBelongingById() {
        // given
        TargetGroupBelonging targetGroupBelonging = TargetGroupBelonging.builder()
            .id(1L).namespaceId(1L).targetId(1L).targetGroupId(1L).createdBy(1L).build();
        when(targetGroupBelongingService.findById(1L)).thenReturn(Mono.just(targetGroupBelonging));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-group-belongings/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroupBelonging.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId, TargetGroupBelonging::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, 1L, 1L));
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係を登録できる")
      void canSaveTheTargetGroupBelonging() {
        // given
        TargetGroupBelonging targetGroupBelonging = TargetGroupBelonging.builder()
            .id(4L).namespaceId(1L).targetId(1L).targetGroupId(3L).createdBy(1L).build();
        when(targetGroupBelongingService.insert(any(TargetGroupBelonging.class))).thenReturn(Mono.just(targetGroupBelonging));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/target-group-belongings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "targetId": 1,
                  "targetGroupId": 3
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroupBelonging.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(TargetGroupBelonging::getId, TargetGroupBelonging::getNamespaceId,
                        TargetGroupBelonging::getTargetId, TargetGroupBelonging::getTargetGroupId, TargetGroupBelonging::getCreatedBy)
                    .containsExactly(4L, 1L, 1L, 3L, 1L));
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係を削除できる")
      void canDeleteTheTargetGroupBelongingById() {
        // given
        when(targetGroupBelongingService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/target-group-belongings/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}