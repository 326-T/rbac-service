package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.Target;
import org.example.persistence.repository.TargetRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TargetService {

  private final TargetRepository targetRepository;

  public TargetService(TargetRepository targetRepository) {
    this.targetRepository = targetRepository;
  }

  public Mono<Target> findById(Long id) {
    return targetRepository.findById(id);
  }

  public Flux<Target> findByNamespaceId(Long namespaceId) {
    return targetRepository.findByNamespaceId(namespaceId);
  }

  public Flux<Target> findByNamespaceIdAndTargetGroupId(Long namespaceId, Long targetGroupId) {
    return targetRepository.findByNamespaceIdAndTargetGroupId(namespaceId, targetGroupId);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param target 保存するTarget
   *
   * @return 保存されたTarget
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<Target> insert(Target target) {
    target.setCreatedAt(LocalDateTime.now());
    target.setUpdatedAt(LocalDateTime.now());
    return targetRepository.findDuplicate(
            target.getNamespaceId(), target.getObjectIdRegex())
        .flatMap(present -> Mono.<Target>error(new RedundantException("Target already exists")))
        .switchIfEmpty(Mono.just(target))
        .flatMap(targetRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 変更内容をセットする
   * 4. 重複がないか確認する
   * 5. 保存する
   *
   * @param target objectIdRegexのみ変更可能
   *
   * @return 更新されたTarget
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<Target> update(Target target) {
    Mono<Target> targetMono = targetRepository.findById(target.getId())
        .filter(present -> Objects.equals(present.getNamespaceId(), target.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Target does not exist in the namespace")))
        .flatMap(present -> {
          present.setObjectIdRegex(target.getObjectIdRegex());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return targetMono
        .flatMap(t -> targetRepository.findDuplicate(t.getNamespaceId(), t.getObjectIdRegex()))
        .flatMap(present -> Mono.<Target>error(new RedundantException("Target already exists")))
        .switchIfEmpty(targetMono)
        .flatMap(targetRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 削除する
   *
   * @param id          TargetのID
   * @param namespaceId TargetのNamespaceId
   *
   * @return Void
   */
  public Mono<Void> deleteById(Long id, Long namespaceId) {
    return targetRepository.findById(id)
        .filter(present -> Objects.equals(present.getNamespaceId(), namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("Target does not exist in the namespace")))
        .map(Target::getId)
        .flatMap(targetRepository::deleteById);
  }
}
