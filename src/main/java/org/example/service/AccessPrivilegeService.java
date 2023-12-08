package org.example.service;

import org.example.persistence.dto.AccessPrivilege;
import org.example.persistence.repository.AccessPrivilegeRepository;
import org.example.web.request.AccessPrivilegeRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccessPrivilegeService {

  private final AccessPrivilegeRepository accessPrivilegeRepository;

  public AccessPrivilegeService(AccessPrivilegeRepository accessPrivilegeRepository) {
    this.accessPrivilegeRepository = accessPrivilegeRepository;
  }

  public Flux<AccessPrivilege> findByNamespace(Long namespaceId) {
    return accessPrivilegeRepository.findByNamespace(namespaceId);
  }

  public Mono<Boolean> canAccess(Long userId, AccessPrivilegeRequest accessPrivilegeRequest) {
    return accessPrivilegeRepository.findByUserAndNamespace(userId, accessPrivilegeRequest.getNamespaceId())
        .any(truth ->
            accessPrivilegeRequest.getMethod().matches(truth.getMethod())
                && accessPrivilegeRequest.getPath().matches(truth.getPathRegex())
                && accessPrivilegeRequest.getObjectId().matches(truth.getObjectIdRegex())
        );
  }
}
