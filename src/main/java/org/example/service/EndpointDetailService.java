package org.example.service;

import org.example.persistence.dto.EndpointDetail;
import org.example.persistence.repository.EndpointDetailRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class EndpointDetailService {

  private final EndpointDetailRepository endpointDetailRepository;


  public EndpointDetailService(EndpointDetailRepository endpointDetailRepository) {
    this.endpointDetailRepository = endpointDetailRepository;
  }

  public Flux<EndpointDetail> findByNamespaceId(Long namespaceId) {
    return endpointDetailRepository.findByNamespaceId(namespaceId);
  }

  public Flux<EndpointDetail> findByNamespaceIdAndRoleId(Long namespaceId, Long roleId) {
    return endpointDetailRepository.findByNamespaceIdAndRoleId(namespaceId, roleId);
  }
}
