package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.repository.EndpointRepository;
import org.example.persistence.repository.PathRepository;
import org.example.persistence.repository.TargetGroupRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EndpointService {

  private final EndpointRepository endpointRepository;
  private final PathRepository pathRepository;
  private final TargetGroupRepository targetGroupRepository;

  public EndpointService(EndpointRepository endpointRepository, PathRepository pathRepository, TargetGroupRepository targetGroupRepository) {
    this.endpointRepository = endpointRepository;
    this.pathRepository = pathRepository;
    this.targetGroupRepository = targetGroupRepository;
  }

  public Flux<Endpoint> findByNamespaceId(Long namespaceId) {
    return endpointRepository.findByNamespaceId(namespaceId);
  }

  public Flux<Endpoint> findByNamespaceIdAndRoleId(Long namespaceId, Long roleId) {
    return endpointRepository.findByNamespaceIdAndRoleId(namespaceId, roleId);
  }

  /**
   * 1. 同じNamespaceIdのPathが存在するか確認する
   * 2. 同じNamespaceIdのTargetGroupが存在するか確認する
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param endpoint 保存するEndpoint
   *
   * @return 保存されたEndpoint
   */
  public Mono<Endpoint> insert(Endpoint endpoint) {
    endpoint.setCreatedAt(LocalDateTime.now());
    endpoint.setUpdatedAt(LocalDateTime.now());
    return pathRepository.findById(endpoint.getPathId())
        .filter(present -> Objects.equals(present.getNamespaceId(), endpoint.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Path does not exist in the namespace")))
        .then(targetGroupRepository.findById(endpoint.getTargetGroupId()))
        .filter(present -> Objects.equals(present.getNamespaceId(), endpoint.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("TargetGroup does not exist in the namespace")))
        .then(endpointRepository.findDuplicate(
            endpoint.getNamespaceId(), endpoint.getPathId(),
            endpoint.getTargetGroupId(), endpoint.getMethod()))
        .flatMap(present -> Mono.<Endpoint>error(new RedundantException("Endpoint already exists")))
        .switchIfEmpty(Mono.just(endpoint))
        .flatMap(endpointRepository::save);
  }

  /**
   * 1. 同じNamespaceIdのPathが存在するか確認する
   * 2. 同じNamespaceIdのTargetGroupが存在するか確認する
   * 3. IDが存在してるか確認する
   * 4. 変更内容をセットする
   * 5. 重複がないか確認する
   * 6. 保存する
   *
   * @param endpoint パスID
   *
   * @return 保存したパス
   */
  public Mono<Endpoint> update(Endpoint endpoint) {
    Mono<Endpoint> endpointMono = pathRepository.findById(endpoint.getPathId())
        .filter(present -> Objects.equals(present.getNamespaceId(), endpoint.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Path does not exist in the namespace")))
        .then(targetGroupRepository.findById(endpoint.getTargetGroupId()))
        .filter(present -> Objects.equals(present.getNamespaceId(), endpoint.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("TargetGroup does not exist in the namespace")))
        .then(endpointRepository.findById(endpoint.getId()))
        .filter(present -> Objects.equals(present.getNamespaceId(), endpoint.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Endpoint does not exist in the namespace")))
        .flatMap(present -> {
          present.setPathId(endpoint.getPathId());
          present.setTargetGroupId(endpoint.getTargetGroupId());
          present.setMethod(endpoint.getMethod());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return endpointMono.flatMap(e -> endpointRepository.findDuplicate(
            e.getNamespaceId(), e.getPathId(), e.getTargetGroupId(), e.getMethod()))
        .flatMap(present -> Mono.<Endpoint>error(new RedundantException("Endpoint already exists")))
        .switchIfEmpty(endpointMono)
        .flatMap(endpointRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 削除する
   *
   * @param id          EndpointのID
   * @param namespaceId EndpointのNamespaceId
   *
   * @return Void
   */
  public Mono<Void> deleteById(Long id, Long namespaceId) {
    return endpointRepository.findById(id)
        .filter(present -> Objects.equals(present.getNamespaceId(), namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("Endpoint does not exist in the namespace")))
        .map(Endpoint::getId)
        .flatMap(endpointRepository::deleteById);
  }
}
