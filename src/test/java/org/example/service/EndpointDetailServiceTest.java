package org.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.example.persistence.dto.EndpointDetail;
import org.example.persistence.repository.EndpointDetailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
class EndpointDetailServiceTest {

  @InjectMocks
  private EndpointDetailService endpointDetailService;
  @Mock
  private EndpointDetailRepository endpointDetailRepository;

  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("namespaceIdでエンドポイントを取得できる")
      void canFindByNamespace() {
        // given
        EndpointDetail endpointDetail1 = EndpointDetail.builder()
            .id(2L).namespaceId(2L).pathId(2L).pathRegex("/billing-service/v1/")
            .targetGroupId(2L).targetGroupName("target-group-2").method("POST")
            .createdBy(2L).build();
        EndpointDetail endpointDetail2 = EndpointDetail.builder()
            .id(3L).namespaceId(2L).pathId(3L).pathRegex("/inventory-service/v2/")
            .targetGroupId(3L).targetGroupName("target-group-3").method("PUT")
            .createdBy(3L).build();
        when(endpointDetailRepository.findByNamespaceId(2L))
            .thenReturn(Flux.just(endpointDetail1, endpointDetail2));
        // when
        Flux<EndpointDetail> endpointDetailFlux = endpointDetailService.findByNamespaceId(2L);
        // then
        StepVerifier.create(endpointDetailFlux)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(
                        EndpointDetail::getId,
                        EndpointDetail::getNamespaceId,
                        EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                        EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                        EndpointDetail::getMethod,
                        EndpointDetail::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, "/billing-service/v1/", 2L, "target-group-2", "POST", 2L))
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(
                        EndpointDetail::getId,
                        EndpointDetail::getNamespaceId,
                        EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                        EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                        EndpointDetail::getMethod,
                        EndpointDetail::getCreatedBy)
                    .containsExactly(3L, 2L, 3L, "/inventory-service/v2/", 3L, "target-group-3", "PUT", 3L))
            .verifyComplete();
      }
    }
  }

  @Nested
  class FindByNamespaceIdAndRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをnamespaceIdとroleIdで取得できる")
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
        when(endpointDetailRepository.findByNamespaceIdAndRoleId(2L, 2L))
            .thenReturn(Flux.just(endpointDetail1, endpointDetail2));
        // when
        Flux<EndpointDetail> endpointDetailFlux = endpointDetailService.findByNamespaceIdAndRoleId(2L, 2L);
        // then
        StepVerifier.create(endpointDetailFlux)
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(
                        EndpointDetail::getId,
                        EndpointDetail::getNamespaceId,
                        EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                        EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                        EndpointDetail::getMethod,
                        EndpointDetail::getCreatedBy)
                    .containsExactly(2L, 2L, 2L, "/billing-service/v1/", 2L, "target-group-2", "POST", 2L))
            .assertNext(
                endpoint -> assertThat(endpoint)
                    .extracting(
                        EndpointDetail::getId,
                        EndpointDetail::getNamespaceId,
                        EndpointDetail::getPathId, EndpointDetail::getPathRegex,
                        EndpointDetail::getTargetGroupId, EndpointDetail::getTargetGroupName,
                        EndpointDetail::getMethod,
                        EndpointDetail::getCreatedBy)
                    .containsExactly(3L, 2L, 3L, "/inventory-service/v2/", 3L, "target-group-3", "PUT", 3L))
            .verifyComplete();
      }
    }
  }
}