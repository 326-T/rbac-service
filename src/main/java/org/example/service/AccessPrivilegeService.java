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

  /**
   * 1. userIdとnamespaceIdから、アクセス権限の一覧を取得する
   * 2. method, path, objectIdの正規表現に全てマッチするものがあるか確認する
   *
   * @param userId                 認証情報
   * @param accessPrivilegeRequest 権限を確認したいリソース
   *
   * @return 権限があるかどうか
   */
  public Mono<Boolean> canAccess(Long userId, AccessPrivilegeRequest accessPrivilegeRequest) {
    return accessPrivilegeRepository.findByUserAndNamespace(userId, accessPrivilegeRequest.getNamespaceId())
        .any(truth ->
            accessPrivilegeRequest.getMethod().matches(truth.getMethod())
                && accessPrivilegeRequest.getPath().matches(truth.getPathRegex())
                && accessPrivilegeRequest.getObjectId().matches(truth.getObjectIdRegex())
        );
  }
}
