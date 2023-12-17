package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.TargetGroup;
import org.example.persistence.entity.User;
import org.example.service.ReactiveContextService;
import org.example.service.TargetGroupService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.request.TargetGroupInsertRequest;
import org.example.web.request.TargetGroupUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    controllers = TargetGroupRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthenticationWebFilter.class)})
@AutoConfigureWebTestClient
class TargetGroupRestControllerTest {

  @MockBean
  private TargetGroupService targetGroupService;
  @MockBean
  private ReactiveContextService reactiveContextService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class Count {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループの件数を取得できる")
      void countTheIndexes() {
        // given
        when(targetGroupService.count()).thenReturn(Mono.just(3L));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/count")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Long.class).isEqualTo(3L);
      }
    }
  }

  @Nested
  class Index {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("target-group-1").createdBy(1L).build();
        TargetGroup targetGroup2 = TargetGroup.builder()
            .id(2L).namespaceId(1L).name("target-group-2").createdBy(2L).build();
        TargetGroup targetGroup3 = TargetGroup.builder()
            .id(3L).namespaceId(1L).name("target-group-3").createdBy(3L).build();
        when(targetGroupService.findByNamespaceId(1L))
            .thenReturn(Flux.just(targetGroup1, targetGroup2, targetGroup3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups?namespace-id=1")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(TargetGroup.class)
            .hasSize(3)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(
                        tuple(1L, 1L, "target-group-1", 1L),
                        tuple(2L, 1L, "target-group-2", 2L),
                        tuple(3L, 1L, "target-group-3", 3L)
                    )
            );
      }
    }
  }

  @Nested
  class FindById {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループをIDで取得できる")
      void canGetTheTargetGroupById() {
        // given
        TargetGroup targetGroup = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("target-group-1").createdBy(1L).build();
        when(targetGroupService.findById(1L)).thenReturn(Mono.just(targetGroup));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId,
                        TargetGroup::getName, TargetGroup::getCreatedBy)
                    .containsExactly(1L, 1L, "target-group-1", 1L));
      }
    }
  }

  @Nested
  class Update {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ターゲットグループを更新できる")
      void canUpdateTheTargetGroup() {
        // given
        TargetGroup targetGroup = TargetGroup.builder()
            .id(2L).namespaceId(1L).name("OBJECT-ID-2").createdBy(1L).build();
        when(targetGroupService.update(any(TargetGroup.class))).thenReturn(Mono.just(targetGroup));
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/target-groups/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "name": "OBJECT-ID-2"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                        TargetGroup::getCreatedBy)
                    .containsExactly(2L, 1L, "OBJECT-ID-2", 1L));
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
        TargetGroupUpdateRequest targetGroupUpdateRequest = new TargetGroupUpdateRequest();
        targetGroupUpdateRequest.setName(name);
        // when, then
        webTestClient.put()
            .uri("/rbac-service/v1/target-groups/2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(targetGroupUpdateRequest)
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
      @DisplayName("ターゲットグループを登録できる")
      void canSaveTheTargetGroup() {
        // given
        TargetGroup targetGroup = TargetGroup.builder()
            .id(4L).namespaceId(1L).name("target-group-4").createdBy(1L).build();
        when(targetGroupService.insert(any(TargetGroup.class))).thenReturn(Mono.just(targetGroup));
        when(reactiveContextService.getCurrentUser()).thenReturn(Mono.just(User.builder().id(1L).build()));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/target-groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "name": "target-group-4"
                }
                """
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(TargetGroup.class)
            .consumeWith(response ->
                assertThat(response.getResponseBody())
                    .extracting(TargetGroup::getId, TargetGroup::getNamespaceId, TargetGroup::getName,
                        TargetGroup::getCreatedBy)
                    .containsExactly(4L, 1L, "target-group-4", 1L));
      }
    }

    @Nested
    @DisplayName("異常系")
    class Error {

      @DisplayName("バリデーションエラーが発生する")
      @ParameterizedTest
      @CsvSource({
          ", target-group-1",
          "0, target-group-1",
          "1, ",
          "1, ''",
          "1, ' '",
      })
      void validationErrorOccurs(Long namespaceId, String name) {
        // given
        TargetGroupInsertRequest targetGroupInsertRequest = new TargetGroupInsertRequest();
        targetGroupInsertRequest.setNamespaceId(namespaceId);
        targetGroupInsertRequest.setName(name);
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/target-groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(targetGroupInsertRequest)
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
      @DisplayName("ターゲットグループを削除できる")
      void canDeleteTheTargetGroupById() {
        // given
        when(targetGroupService.deleteById(3L)).thenReturn(Mono.empty());
        // when, then
        webTestClient.delete()
            .uri("/rbac-service/v1/target-groups/3")
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();
      }
    }
  }
}