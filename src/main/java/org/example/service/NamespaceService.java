package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Namespace;
import org.example.persistence.repository.NamespaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NamespaceService {

  private final NamespaceRepository namespaceRepository;

  public NamespaceService(NamespaceRepository namespaceRepository) {
    this.namespaceRepository = namespaceRepository;
  }

  public Mono<Long> count() {
    return namespaceRepository.count();
  }

  public Flux<Namespace> findAll() {
    return namespaceRepository.findAll();
  }

  public Mono<Namespace> findById(Long id) {
    return namespaceRepository.findById(id);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param namespace 保存するNamespace
   *
   * @return 保存されたNamespace
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<Namespace> insert(Namespace namespace) {
    namespace.setCreatedAt(LocalDateTime.now());
    namespace.setUpdatedAt(LocalDateTime.now());
    return namespaceRepository.findDuplicate(namespace.getName())
        .flatMap(present -> Mono.<Namespace>error(new RedundantException("Namespace already exists")))
        .switchIfEmpty(Mono.just(namespace))
        .flatMap(namespaceRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. 変更内容をセットする
   * 3. 重複がないか確認する
   * 4. 保存する
   *
   * @param namespace nameのみ変更可能
   *
   * @return 更新されたNamespace
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<Namespace> update(Namespace namespace) {
    Mono<Namespace> namespaceMono = namespaceRepository.findById(namespace.getId())
        .switchIfEmpty(Mono.error(new NotExistingException("Namespace not found")))
        .flatMap(present -> {
          present.setName(namespace.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return namespaceMono.flatMap(e -> namespaceRepository.findDuplicate(e.getName()))
        .flatMap(present -> Mono.<Namespace>error(new RedundantException("Namespace already exists")))
        .switchIfEmpty(namespaceMono)
        .flatMap(namespaceRepository::save);
  }

  public Mono<Void> deleteById(Long id) {
    return namespaceRepository.deleteById(id);
  }
}
