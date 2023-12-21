package org.example.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.persistence.dto.EndpointDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
class EndpointDetailRepositoryTest {

  @Autowired
  private EndpointDetailRepository endpointDetailRepository;

  @Order(1)
  @Nested
  class FindByNamespaceId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをnamespaceIdで取得できる")
      void findAllTheIndexes() {
        // when
        Flux<EndpointDetail> endpointFlux = endpointDetailRepository.findByNamespaceId(2L);
        // then
        StepVerifier.create(endpointFlux)
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

  @Order(1)
  @Nested
  class FindByNamespaceIdAndRoleId {

    @Nested
    @DisplayName("正常系")
    class Regular {

      @Test
      @DisplayName("エンドポイントをnamespaceIdとroleIdで取得できる")
      void canFindByNamespaceIdAndRoleId() {
        // when
        Flux<EndpointDetail> endpointFlux = endpointDetailRepository.findByNamespaceIdAndRoleId(2L, 2L);
        // then
        StepVerifier.create(endpointFlux)
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
            .verifyComplete();
      }
    }
  }
}