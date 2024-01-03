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
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.TargetInsertRequest;
import org.example.web.request.TargetUpdateRequest;
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
    controllers = TargetRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class TargetRestControllerTest {

  @MockBean
  private TargetService targetService;
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
      @DisplayName("ターゲットをnamespace-idで取得できる")
      void findAllByNamespaceId() {
        // given
        Target target1 = Target.builder()
            .id(1L).namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        Target target2 = Target.builder()
            .id(2L).namespaceId(1L).objectIdRegex("object-id-2").createdBy(2L).build();
        Target target3 = Target.builder()
            .id(3L).namespaceId(1L).objectIdRegex("object-id-3").createdBy(3L).build();
        when(targetService.findByNamespaceId(1L)).thenReturn(Flux.just(target1, target2, target3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/targets")
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
                        tuple(2L, 1L, "object-id-2", 2L),
                        tuple(3L, 1L, "object-id-3", 3L)
                    )
            );
      }

      @Test
      @DisplayName("ターゲットをnamespace-idとtarget-group-idで取得できる")
      void findAllByNamespaceIdAndUserGroupId() {
        // given
        Target target1 = Target.builder()
            .id(1L).namespaceId(1L).objectIdRegex("object-id-1").createdBy(1L).build();
        Target target2 = Target.builder()
            .id(2L).namespaceId(1L).objectIdRegex("object-id-2").createdBy(2L).build();
        Target target3 = Target.builder()
            .id(3L).namespaceId(1L).objectIdRegex("object-id-3").createdBy(3L).build();
        when(targetService.findByNamespaceIdAndTargetGroupId(1L, 1L))
            .thenReturn(Flux.just(target1, target2, target3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/targets?target-group-id=1")
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
                        tuple(2L, 1L, "object-id-2", 2L),
                        tuple(3L, 1L, "object-id-3", 3L)
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
      @DisplayName("ターゲットを更新できる")
      void canUpdateTheTarget() {
        // given
        Target target = Target.builder()
            .id(2L).namespaceId(1L).objectIdRegex("OBJECT-ID-2").createdBy(1L).build();
        when(targetService.update(any(Target.class))).thenReturn(Mono.just(target));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/targets/2")
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

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String objectIdRegex) {
        // given
        TargetUpdateRequest targetUpdateRequest = new TargetUpdateRequest();
        targetUpdateRequest.setObjectIdRegex(objectIdRegex);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/targets/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(targetUpdateRequest)
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
      @DisplayName("ターゲットを登録できる")
      void canSaveTheTarget() {
        // given
        Target target = Target.builder()
            .id(4L).namespaceId(1L).objectIdRegex("object-id-4").createdBy(1L).build();
        when(targetService.insert(any(Target.class))).thenReturn(Mono.just(target));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/targets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
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

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @ValueSource(strings = {"", " "})
      void validationErrorOccurs(String objectIdRegex) {
        // given
        TargetInsertRequest targetInsertRequest = new TargetInsertRequest();
        targetInsertRequest.setObjectIdRegex(objectIdRegex);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/targets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(targetInsertRequest)
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
      @DisplayName("ターゲットを削除できる")
      void canDeleteTheTargetById() {
        // given
        when(targetService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/targets/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}