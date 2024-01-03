package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetGroup;
import org.example.persistence.repository.TargetGroupRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TargetGroupService {

  private final TargetGroupRepository targetGroupRepository;

  public TargetGroupService(TargetGroupRepository targetGroupRepository) {
    this.targetGroupRepository = targetGroupRepository;
  }

  public Flux<TargetGroup> findByNamespaceId(Long namespaceId) {
    return targetGroupRepository.findByNamespaceId(namespaceId);
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param targetGroup 保存するTargetGroup
   *
   * @return 保存されたTargetGroup
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<TargetGroup> insert(TargetGroup targetGroup) {
    targetGroup.setCreatedAt(LocalDateTime.now());
    targetGroup.setUpdatedAt(LocalDateTime.now());
    return targetGroupRepository.findDuplicate(
            targetGroup.getNamespaceId(), targetGroup.getName())
        .flatMap(present -> Mono.<TargetGroup>error(new RedundantException("TargetGroup already exists")))
        .switchIfEmpty(Mono.just(targetGroup))
        .flatMap(targetGroupRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 変更内容をセットする
   * 4. 重複がないか確認する
   * 5. 保存する
   *
   * @param targetGroup nameのみ変更可能
   *
   * @return 更新されたTargetGroup
   *
   * @throws NotExistingException IDが存在しない場合
   * @throws RedundantException   重複した場合
   */
  public Mono<TargetGroup> update(TargetGroup targetGroup, Long namespaceId) {
    Mono<TargetGroup> targetGroupMono = targetGroupRepository.findById(targetGroup.getId())
        .filter(t -> t.getNamespaceId().equals(namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("TargetGroup not found")))
        .flatMap(present -> {
          present.setName(targetGroup.getName());
          present.setUpdatedAt(LocalDateTime.now());
          return Mono.just(present);
        });
    return targetGroupMono
        .flatMap(tg -> targetGroupRepository.findDuplicate(tg.getNamespaceId(), tg.getName()))
        .flatMap(present -> Mono.<TargetGroup>error(new RedundantException("TargetGroup already exists")))
        .switchIfEmpty(targetGroupMono)
        .flatMap(targetGroupRepository::save);
  }

  /**
   * 1. IDが存在してるか確認する
   * 2. NamespaceIdが一致しているか確認する
   * 3. 削除する
   *
   * @param id          TargetGroupのID
   * @param namespaceId TargetGroupのNamespaceId
   *
   * @return Void
   */
  public Mono<Void> deleteById(Long id, Long namespaceId) {
    return targetGroupRepository.findById(id)
        .filter(t -> t.getNamespaceId().equals(namespaceId))
        .switchIfEmpty(Mono.error(new NotExistingException("TargetGroup not found")))
        .flatMap(tg -> targetGroupRepository.deleteById(tg.getId()));
  }
}
