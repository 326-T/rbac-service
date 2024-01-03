package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.dto.EndpointDetail;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.entity.User;
import org.example.service.EndpointDetailService;
import org.example.service.EndpointService;
import org.example.service.ReactiveContextService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.example.web.request.EndpointInsertRequest;
import org.example.web.request.EndpointUpdateRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(
    controllers = EndpointRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class EndpointRestControllerTest {

  @MockBean
  private EndpointService endpointService;
  @MockBean
  private EndpointDetailService endpointDetailService;
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
      @DisplayName("エンドポイントをnamespaceIDで取得できる")
      void canFindByNamespaceId() {
        // given
        EndpointDetail endpointDetail1 = EndpointDetail.builder()
            .id(2L).namespaceId(2L).pathId(2L).pathRegex("/billing-service/v1/")
            .targetGroupId(2L).targetGroupName("target-group-2").method("POST")
            .createdBy(2L).build();
        EndpointDetail endpointDetail2 = EndpointDetail.builder()
            .id(3L).namespaceId(2L).pathId(3L).pathRegex("/inventory-service/v2/")
            .targetGroupId(3L).targetGroupName("target-group-3").method("PUT")
            .createdBy(3L).build();
        when(endpointDetailService.findByNamespaceId(2L))
            .thenReturn(Flux.just(endpointDetail1, endpointDetail2));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/endpoints")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(EndpointDetail.class)
            .hasSize(2)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(
                        EndpointDetail::getId,
                        EndpointDetail::getNamespaceId,
                        EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                        EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                        EndpointDetail::getMethod,
                        EndpointDetail::getCreatedBy)
                    .containsExactly(
                        tuple(2L, 2L, 2L, "/billing-service/v1/", 2L, "target-group-2", "POST", 2L),
                        tuple(3L, 2L, 3L, "/inventory-service/v2/", 3L, "target-group-3", "PUT", 3L)
                    )
            );
      }

      @Test
      @DisplayName("エンドポイントをnamespaceIDとroleIDで取得できる")
      void canFindByNamespaceIdAndRoleId() {
        // given
        EndpointDetail endpointDetail1 = EndpointDetail.builder()
            .id(2L).namespaceId(2L).pathId(2L).pathRegex("/billing-service/v1/")
            .targetGroupId(2L).targetGroupName("target-group-2").method("POST")
            .createdBy(2L).build();
        EndpointDetail endpointDetail2 = EndpointDetail.builder()
            .id(3L).namespaceId(2L).pathId(3L).pathRegex("/inventory-service/v2/")
            .targetGroupId(3L).targetGroupName("target-group-3").method("PUT")
            .createdBy(3L).build();
        when(endpointDetailService.findByNamespaceIdAndRoleId(2L, 2L))
            .thenReturn(Flux.just(endpointDetail1, endpointDetail2));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/2/endpoints?role-id=2")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(EndpointDetail.class)
            .hasSize(2)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(
                        EndpointDetail::getId,
                        EndpointDetail::getNamespaceId,
                        EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                        EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                        EndpointDetail::getMethod,
                        EndpointDetail::getCreatedBy)
                    .containsExactly(
                        tuple(2L, 2L, 2L, "/billing-service/v1/", 2L, "target-group-2", "POST", 2L),
                        tuple(3L, 2L, 3L, "/inventory-service/v2/", 3L, "target-group-3", "PUT", 3L)
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
      @DisplayName("エンドポイントを更新できる")
      void canUpdateTheEndpoint() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(2L).namespaceId(2L).pathId(3L).method("GET").targetGroupId(2L).createdBy(1L)
            .build();
        when(endpointService.update(any(Endpoint.class))).thenReturn(Mono.just(endpoint));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
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

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", 'GET', 1",
          "0, 'GET', 1",
          "1, , 1",
          "1, '', 1",
          "1, ' ', 1",
          "1, 'GET', ",
          "1, 'GET', 0",
      })
      void validationErrorOccurs(Long pathId, String method, Long targetGroupId) {
        // given
        EndpointUpdateRequest endpointUpdateRequest = new EndpointUpdateRequest();
        endpointUpdateRequest.setPathId(pathId);
        endpointUpdateRequest.setTargetGroupId(targetGroupId);
        endpointUpdateRequest.setMethod(method);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/2/endpoints/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(endpointUpdateRequest)
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
      @DisplayName("エンドポイントを登録できる")
      void canSaveTheEndpoint() {
        // given
        Endpoint endpoint = Endpoint.builder()
            .id(4L).namespaceId(2L).pathId(3L).method("GET").targetGroupId(2L).createdBy(1L)
            .build();
        when(endpointService.insert(any(Endpoint.class))).thenReturn(Mono.just(endpoint));
        when(reactiveContextService.extractCurrentUser(any(ServerWebExchange.class))).thenReturn(User.builder().id(1L).build());
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/2/endpoints")
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

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", 1, GET",
          "0, 1, GET",
          "1, , GET",
          "1, 0, GET",
          "1, 1, ",
          "1, 1, ''",
          "1, 1, ' '",
      })
      void validationErrorOccurs(Long pathId, Long targetGroupId, String method) {
        // given
        EndpointInsertRequest endpointInsertRequest = new EndpointInsertRequest();
        endpointInsertRequest.setPathId(pathId);
        endpointInsertRequest.setTargetGroupId(targetGroupId);
        endpointInsertRequest.setMethod(method);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/1/endpoints")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(endpointInsertRequest)
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
      @DisplayName("エンドポイントを削除できる")
      void canDeleteTheEndpointById() {
        // given
        when(endpointService.deleteById(3L, 1L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/1/endpoints/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}