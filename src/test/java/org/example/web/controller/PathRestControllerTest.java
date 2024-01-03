package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Path;
import org.example.persistence.entity.User;
import org.example.service.PathService;
import org.example.service.ReactiveContextService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.PathInsertRequest;
import org.example.web.request.PathUpdateRequest;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = PathRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class PathRestControllerTest {

  @MockBean
  private PathService pathService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("パスを全件取得できる")
      void findAllTheIndexes() {
        // given
        Path path1 = Path.builder()
            .id(1L).namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        Path path2 = Path.builder()
            .id(2L).namespaceId(1L).regex("/billing-service/v1").createdBy(2L).build();
        Path path3 = Path.builder()
            .id(3L).namespaceId(1L).regex("/movie-service/v1").createdBy(3L).build();
        when(pathService.findByNamespaceId(1L)).thenReturn(Flux.just(path1, path2, path3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/paths")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Path.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, "/user-service/v1", 1L),
                        tuple(2L, 1L, "/billing-service/v1", 2L),
                        tuple(3L, 1L, "/movie-service/v1", 3L)
                    )
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
      @DisplayName("パスを更新できる")
      void canUpdateThePath() {
        // given
        Path path = Path.builder()
            .id(2L).namespaceId(1L).regex("/replace-service/v1/").createdBy(1L).build();
        when(pathService.update(any(Path.class))).thenReturn(Mono.just(path));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/1/paths/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "regex": "/replace-service/v1/"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(2L, 1L, "/replace-service/v1/", 1L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String regex) {
        // given
        PathInsertRequest pathInsertRequest = new PathInsertRequest();
        pathInsertRequest.setRegex(regex);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/paths")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(pathInsertRequest)
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
      @DisplayName("パスを登録できる")
      void canSaveThePath() {
        // given
        Path path = Path.builder()
            .id(4L).namespaceId(1L).regex("/next-service/v1/").createdBy(1L).build();
        when(pathService.insert(any(Path.class))).thenReturn(Mono.just(path));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/paths")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "regex": "/next-service/v1/"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId, Path::getRegex,
                        Path::getCreatedBy)
                    .containsExactly(4L, 1L, "/next-service/v1/", 1L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String regex) {
        // given
        PathUpdateRequest pathUpdateRequest = new PathUpdateRequest();
        pathUpdateRequest.setRegex(regex);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/1/paths/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(pathUpdateRequest)
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
      @DisplayName("パスを削除できる")
      void canDeleteThePathById() {
        // given
        when(pathService.deleteById(3L, 1L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/paths/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}