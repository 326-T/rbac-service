package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;
import org.example.error.exception.NotExistingException;
import org.example.error.exception.RedundantException;
import org.example.persistence.entity.TargetGroupBelonging;
import org.example.persistence.repository.TargetGroupBelongingRepository;
import org.example.persistence.repository.TargetGroupRepository;
import org.example.persistence.repository.TargetRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TargetGroupBelongingService {

  private final TargetGroupBelongingRepository targetGroupBelongingRepository;
  private final TargetRepository targetRepository;
  private final TargetGroupRepository targetGroupRepository;

  public TargetGroupBelongingService(
      TargetGroupBelongingRepository targetGroupBelongingRepository, TargetRepository targetRepository, TargetGroupRepository targetGroupRepository) {
    this.targetGroupBelongingRepository = targetGroupBelongingRepository;
    this.targetRepository = targetRepository;
    this.targetGroupRepository = targetGroupRepository;
  }

  /**
   * 1. 同じNamespaceIdのTargetが存在するか確認する
   * 2. 同じNamespaceIdのTargetGroupが存在するか確認する
   * 3. 重複がないか確認する
   * 4. 保存する
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
    return targetRepository.findById(targetGroupBelonging.getTargetId())
        .filter(t -> Objects.equals(t.getNamespaceId(), targetGroupBelonging.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("Target is not in the namespace")))
        .then(targetGroupRepository.findById(targetGroupBelonging.getTargetGroupId()))
        .filter(tg -> Objects.equals(tg.getNamespaceId(), targetGroupBelonging.getNamespaceId()))
        .switchIfEmpty(Mono.error(new NotExistingException("TargetGroup is not in the namespace")))
        .then(targetGroupBelongingRepository.findDuplicate(
            targetGroupBelonging.getNamespaceId(),
            targetGroupBelonging.getTargetGroupId(), targetGroupBelonging.getTargetId()))
        .flatMap(present -> Mono.<TargetGroupBelonging>error(new RedundantException("TargetGroupBelonging already exists")))
        .switchIfEmpty(Mono.just(targetGroupBelonging))
        .flatMap(targetGroupBelongingRepository::save);
  }

  public Mono<Void> deleteByUniqueKeys(Long namespaceId, Long targetId, Long targetGroupId) {
    return targetGroupBelongingRepository.deleteByUniqueKeys(namespaceId, targetId, targetGroupId);
  }
}
