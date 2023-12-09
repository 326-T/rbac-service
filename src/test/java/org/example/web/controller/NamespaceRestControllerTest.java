package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Namespace;
import org.example.persistence.entity.User;
import org.example.service.NamespaceService;
import org.example.service.ReactiveContextService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.request.NamespaceUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    controllers = NamespaceRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class NamespaceRestControllerTest {

  @MockBean
  private NamespaceService namespaceService;
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
      @DisplayName("ネームスペースの件数を取得できる")
      void countTheIndexes() {
        // given
        when(namespaceService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/namespaces/count")
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
      @DisplayName("ネームスペースを全件取得できる")
      void findAllTheIndexes() {
        // given
        Namespace namespace1 = Namespace.builder()
            .id(1L).name("front").createdBy(1L).build();
        Namespace namespace2 = Namespace.builder()
            .id(2L).name("backend").createdBy(2L).build();
        Namespace namespace3 = Namespace.builder()
            .id(3L).name("database").createdBy(3L).build();
        when(namespaceService.findAll()).thenReturn(Flux.just(namespace1, namespace2, namespace3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/namespaces")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Namespace.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(
                        tuple(1L, "front", 1L),
                        tuple(2L, "backend", 2L),
                        tuple(3L, "database", 3L)
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
      @DisplayName("ネームスペースをIDで取得できる")
      void canGetTheNamespaceById() {
        // given
        Namespace namespace = Namespace.builder()
            .id(1L).name("front").createdBy(1L).build();
        when(namespaceService.findById(1L)).thenReturn(Mono.just(namespace));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/namespaces/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(1L, "front", 1L));
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
      void canUpdateTheNamespace() {
        // given
        Namespace namespace = Namespace.builder()
            .id(2L).name("kvs").createdBy(1L).build();
        when(namespaceService.update(any(Namespace.class))).thenReturn(Mono.just(namespace));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/namespaces/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "kvs"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(2L, "kvs", 1L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String name) {
        // given
        NamespaceUpdateRequest namespaceUpdateRequest = new NamespaceUpdateRequest();
        namespaceUpdateRequest.setName(name);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/namespaces/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(namespaceUpdateRequest)
            .exchange()
            .expectStatus().isBadRequest();
      }
    }
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを登録できる")
      void canSaveTheNamespace() {
        // given
        Namespace namespace = Namespace.builder()
            .id(4L).name("vault").createdBy(1L).build();
        when(namespaceService.insert(any(Namespace.class))).thenReturn(Mono.just(namespace));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "vault"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Namespace.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Namespace::getId, Namespace::getName, Namespace::getCreatedBy)
                    .containsExactly(4L, "vault", 1L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String name) {
        // given
        NamespaceUpdateRequest namespaceUpdateRequest = new NamespaceUpdateRequest();
        namespaceUpdateRequest.setName(name);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/namespaces")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(namespaceUpdateRequest)
            .exchange()
            .expectStatus().isBadRequest();
      }
    }
  }

  @Nested
  class DeleteById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースを削除できる")
      void canDeleteTheNamespaceById() {
        // given
        when(namespaceService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/namespaces/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}