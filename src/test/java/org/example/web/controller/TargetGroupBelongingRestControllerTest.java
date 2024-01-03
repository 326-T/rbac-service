package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.TargetGroupBelonging;
import org.example.persistence.entity.User;
import org.example.service.ReactiveContextService;
import org.example.service.TargetGroupBelongingService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.TargetGroupBelongingInsertRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = TargetGroupBelongingRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class TargetGroupBelongingRestControllerTest {

  @MockBean
  private TargetGroupBelongingService targetGroupBelongingService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

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
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/target-group-belongings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
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

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", 1",
          "0, 1",
          "1, ",
          "1, 0",
      })
      void validationErrorOccurs(Long targetId, Long targetGroupId) {
        // given
        TargetGroupBelongingInsertRequest targetGroupBelongingInsertRequest = new TargetGroupBelongingInsertRequest();
        targetGroupBelongingInsertRequest.setTargetId(targetId);
        targetGroupBelongingInsertRequest.setTargetGroupId(targetGroupId);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/target-group-belongings")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(targetGroupBelongingInsertRequest)
            .exchange()
            .expectStatus().isBadRequest();
      }
    }
  }

  @Nested
  class DeleteByUniqueKeys {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットとグループの関係を名前空間IDとターゲットIDとターゲットグループIDで削除できる")
      void canDeleteTheTargetGroupBelongingByUniqueKeys() {
        // given
        when(targetGroupBelongingService.deleteByUniqueKeys(1L, 2L, 3L))
            .thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/target-group-belongings?target-id=2&target-group-id=3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}