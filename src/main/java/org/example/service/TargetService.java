package org.example.service;

import java.time.LocalDateTime;
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
   * 2. 変更内容をセットする
   * 3. 重複がないか確認する
   * 4. 保存する
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
        .switchIfEmpty(Mono.error(new NotExistingException("Target not found")))
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

  public Mono<Void> deleteById(Long id) {
    return targetRepository.deleteById(id);
  }
}
