package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.Path;
import org.example.service.PathService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(PathRestController.class)
@AutoConfigureWebTestClient
class PathRestControllerTest {

  @MockBean
  private PathService pathService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("パスの件数を取得できる")
      void countTheIndexes() {
        // given
        when(pathService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths/count")
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
      @DisplayName("パスを全件取得できる")
      void findAllTheIndexes() {
        // given
        Path path1 = Path.builder()
            .id(1L).namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        Path path2 = Path.builder()
            .id(2L).namespaceId(2L).regex("/billing-service/v1").createdBy(2L).build();
        Path path3 = Path.builder()
            .id(3L).namespaceId(3L).regex("/movie-service/v1").createdBy(3L).build();
        when(pathService.findAll()).thenReturn(Flux.just(path1, path2, path3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths")
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
                        tuple(2L, 2L, "/billing-service/v1", 2L),
                        tuple(3L, 3L, "/movie-service/v1", 3L)
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
      @DisplayName("パスをIDで取得できる")
      void canGetThePathById() {
        // given
        Path path = Path.builder()
            .id(1L).namespaceId(1L).regex("/user-service/v1").createdBy(1L).build();
        when(pathService.findById(1L)).thenReturn(Mono.just(path));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/paths/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Path.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(Path::getId, Path::getNamespaceId,
                        Path::getRegex, Path::getCreatedBy)
                    .containsExactly(1L, 1L, "/user-service/v1", 1L));
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("パスを更新できる")
      void canUpdateThePath() {
        // given
        Path path = Path.builder()
            .id(2L).namespaceId(1L).regex("/replace-service/v1/").createdBy(1L).build();
        when(pathService.update(any(Path.class))).thenReturn(Mono.just(path));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/paths/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "regex": "/replace-service/v1/",
                  "createdBy": 1
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
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("パスを登録できる")
      void canSaveThePath() {
        // given
        Path path = Path.builder()
            .id(4L).namespaceId(1L).regex("/next-service/v1/").createdBy(1L).build();
        when(pathService.insert(any(Path.class))).thenReturn(Mono.just(path));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/paths")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "regex": "/next-service/v1/",
                  "createdBy": 1
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
  }

  @Nested
  class deleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("パスを削除できる")
      void canDeleteThePathById() {
        // given
        when(pathService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/paths/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}