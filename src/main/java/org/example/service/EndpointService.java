package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Endpoint;
import org.example.persistence.repository.EndpointRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EndpointService {

  private final EndpointRepository endpointRepository;

  public EndpointService(EndpointRepository endpointRepository) {
    this.endpointRepository = endpointRepository;
  }

  public Mono<Long> count() {
    return endpointRepository.count();
  }

  public Flux<Endpoint> findAll() {
    return endpointRepository.findAll();
  }

  public Mono<Endpoint> findById(Long id) {
    return endpointRepository.findById(id);
  }

  public Mono<Endpoint> insert(Endpoint endpoint) {
    endpoint.setCreatedAt(LocalDateTime.now());
    endpoint.setUpdatedAt(LocalDateTime.now());
    return endpointRepository.findDuplicate(
            endpoint.getNamespaceId(), endpoint.getPathId(),
            endpoint.getTargetGroupId(), endpoint.getMethod())
        .flatMap(present -> Mono.<Endpoint>error(new RedundantException("Endpoint already exists")))
        .switchIfEmpty(Mono.just(endpoint))
        .flatMap(endpointRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. 変更内容をセットする
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param endpoint パスID
   *
   * @return
   */
  public Mono<Endpoint> update(Endpoint endpoint) {
    Mono<Endpoint> endpointMono = endpointRepository.findById(endpoint.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("Endpoint not found")))
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

  public Mono<Void> deleteById(Long id) {
    return endpointRepository.deleteById(id);
  }
}
