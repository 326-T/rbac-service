package org.example.service;

import java.time.LocalDateTime;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetGroupBelonging;
import org.example.persistence.repository.TargetGroupBelongingRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TargetGroupBelongingService {

  private final TargetGroupBelongingRepository targetGroupBelongingRepository;

  public TargetGroupBelongingService(
      TargetGroupBelongingRepository targetGroupBelongingRepository) {
    this.targetGroupBelongingRepository = targetGroupBelongingRepository;
  }

  /**
   * 1. 重複がないか確認する
   * 2. 保存する
   *
   * @param targetGroupBelonging 保存するTargetGroupBelonging
   *
   * @return 保存されたTargetGroupBelonging
   *
   * @throws RedundantException 重複した場合
   */
  public Mono<TargetGroupBelonging> insert(TargetGroupBelonging targetGroupBelonging) {
    targetGroupBelonging.setCreatedAt(LocalDateTime.now());
    targetGroupBelonging.setUpdatedAt(LocalDateTime.now());
    return targetGroupBelongingRepository.findDuplicate(
            targetGroupBelonging.getNamespaceId(),
            targetGroupBelonging.getTargetGroupId(), targetGroupBelonging.getTargetId())
        .flatMap(present -> Mono.<TargetGroupBelonging>error(new RedundantException("TargetGroupBelonging already exists")))
        .switchIfEmpty(Mono.just(targetGroupBelonging))
        .flatMap(targetGroupBelongingRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long targetId, Long targetGroupId) {
    return targetGroupBelongingRepository.deleteByUniqueKeys(namespaceId, targetId, targetGroupId);
  }
}
