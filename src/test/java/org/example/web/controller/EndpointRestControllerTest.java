package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Endpoint;
import org.example.persistence.entity.User;
import org.example.service.EndpointService;
import org.example.service.ReactiveContextService;
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
    controllers = EndpointRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class EndpointRestControllerTest {

  @MockBean
  private EndpointService endpointService;
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
      @DisplayName("エンドポイントの件数を取得できる")
      void countTheIndexes() {
        // given
        when(endpointService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/count")
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
      @DisplayName("エンドポイントを全件取得できる")
      void findAllTheIndexes() {
        // given
        Endpoint endpoint1 = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L)
            .build();
        Endpoint endpoint2 = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(2L).method("POST").targetGroupId(2L).createdBy(2L)
            .build();
        Endpoint endpoint3 = Endpoint.builder()
            .id(3L).namespaceId(3L).pathId(3L).method("PUT").targetGroupId(3L).createdBy(3L)
            .build();
        when(endpointService.findAll()).thenReturn(Flux.just(endpoint1, endpoint2, endpoint3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Endpoint.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, 1L, "GET", 1L, 1L),
                        tuple(2L, 2L, 2L, "POST", 2L, 2L),
                        tuple(3L, 3L, 3L, "PUT", 3L, 3L)
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
      @DisplayName("エンドポイントをIDで取得できる")
      void canGetTheEndpointById() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(1L).namespaceId(1L).pathId(1L).method("GET").targetGroupId(1L).createdBy(1L)
            .build();
        when(endpointService.findById(1L)).thenReturn(Mono.just(endpoint));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/endpoints/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                        Endpoint::getPathId, Endpoint::getMethod,
                        Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                    .containsExactly(1L, 1L, 1L, "GET", 1L, 1L)
            );
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを更新できる")
      void canUpdateTheEndpoint() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(3L).method("GET").targetGroupId(2L).createdBy(1L)
            .build();
        when(endpointService.update(any(Endpoint.class))).thenReturn(Mono.just(endpoint));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/endpoints/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(2L, 2L, 3L, "GET", 2L, 1L);
            });
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを登録できる")
      void canSaveTheEndpoint() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(4L).namespaceId(2L).pathId(3L).method("GET").targetGroupId(2L).createdBy(1L)
            .build();
        when(endpointService.insert(any(Endpoint.class))).thenReturn(Mono.just(endpoint));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/endpoints")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "pathId": 3,
                  "method": "GET",
                  "targetGroupId": 2
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Endpoint.class)
            .consumeWith(response -> {
              assertThat(response.getResponseBody())
                  .extracting(Endpoint::getId, Endpoint::getNamespaceId,
                      Endpoint::getPathId, Endpoint::getMethod,
                      Endpoint::getTargetGroupId, Endpoint::getCreatedBy)
                  .containsExactly(4L, 2L, 3L, "GET", 2L, 1L);
            });
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントを削除できる")
      void canDeleteTheEndpointById() {
        // given
        when(endpointService.deleteById(1L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/endpoints/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}