package org.example.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import org.example.persistence.entity.SystemRole;
import org.example.service.SystemRoleService;
import org.example.web.filter.AuthenticationWebFilter;
import org.example.web.filter.AuthorizationWebFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(
    controllers = SystemRoleRestController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthenticationWebFilter.class, AuthorizationWebFilter.class})})
@AutoConfigureWebTestClient
class SystemRoleRestControllerTest {

  @MockBean
  private SystemRoleService systemRoleService;
  @Autowired
  private WebTestClient webTestClient;

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("ネームスペースIDでシステムロールを取得できる")
      void findByNamespaceId() {
        // given
        SystemRole developRead = SystemRole.builder()
            .id(1L)
            .name("develop_READ")
            .namespaceId(1L)
            .permission("READ")
            .build();
        SystemRole developWrite = SystemRole.builder()
            .id(2L)
            .name("develop_WRITE")
            .namespaceId(1L)
            .permission("WRITE")
            .build();
        when(systemRoleService.findByNamespaceId(1L))
            .thenReturn(Flux.just(developRead, developWrite));
        // when, then
        webTestClient.get()
            .uri("/rbac-service/v1/1/system-roles")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(SystemRole.class)
            .hasSize(2)
            .consumeWith(result ->
                assertThat(result.getResponseBody())
                    .extracting(
                        SystemRole::getId, SystemRole::getName,
                        SystemRole::getNamespaceId, SystemRole::getPermission)
                    .containsExactly(
                        tuple(1L, "develop_READ", 1L, "READ"),
                        tuple(2L, "develop_WRITE", 1L, "WRITE")
                    )
            );
      }

    }
  }
}