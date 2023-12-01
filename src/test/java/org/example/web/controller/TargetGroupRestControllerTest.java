package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.TargetGroup;
import org.example.service.TargetGroupService;
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

@WebFluxTest(TargetGroupRestController.class)
@AutoConfigureWebTestClient
class TargetGroupRestControllerTest {

  @MockBean
  private TargetGroupService targetGroupService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class index {

    @Nested
    @DisplayName("正常系")
    class regular {

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
  class findAll {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを全件取得できる")
      void findAllTheIndexes() {
        // given
        TargetGroup targetGroup1 = TargetGroup.builder()
            .id(1L).namespaceId(1L).name("target-group-1").createdBy(1L).build();
        TargetGroup targetGroup2 = TargetGroup.builder()
            .id(2L).namespaceId(2L).name("target-group-2").createdBy(2L).build();
        TargetGroup targetGroup3 = TargetGroup.builder()
            .id(3L).namespaceId(3L).name("target-group-3").createdBy(3L).build();
        when(targetGroupService.findAll()).thenReturn(Flux.just(targetGroup1, targetGroup2, targetGroup3));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/target-groups")
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
                        tuple(2L, 2L, "target-group-2", 2L),
                        tuple(3L, 3L, "target-group-3", 3L)
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
    class regular {

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
                  "namespaceId": 1,
                  "name": "OBJECT-ID-2",
                  "createdBy": 1
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
  }

  @Nested
  class Save {

    @Nested
    @DisplayName("正常系")
    class regular {

      @Test
      @DisplayName("ターゲットグループを登録できる")
      void canSaveTheTargetGroup() {
        // given
        TargetGroup targetGroup = TargetGroup.builder()
            .id(4L).namespaceId(1L).name("target-group-4").createdBy(1L).build();
        when(targetGroupService.insert(any(TargetGroup.class))).thenReturn(Mono.just(targetGroup));
        // when, then
        webTestClient.post()
            .uri("/rbac-service/v1/target-groups")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                  "namespaceId": 1,
                  "name": "target-group-4",
                  "createdBy": 1
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
  }

  @Nested
  class deleteById {

    @Nested
    @DisplayName("正常系")
    class regular {

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