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

  public Mono<Boolean> canAccess(AccessPrivilegeRequest ask) {
    return accessPrivilegeRepository.findByUser(ask.getUserId())
        .any(truth ->
            ask.getNamespaceId().equals(truth.getNamespaceId())
                && ask.getMethod().equals(truth.getMethod())
                && ask.getPath().matches(truth.getPathRegex())
                && ask.getObjectId().matches(truth.getObjectIdRegex())
        );
  }
}
