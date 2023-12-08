package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.TargetGroupBelonging;

@NoArgsConstructor
@Data
public class TargetGroupBelongingInsertRequest {

  private Long namespaceId;
  private Long targetId;
  private Long targetGroupId;

  public TargetGroupBelonging exportEntity() {
    return TargetGroupBelonging.builder()
        .namespaceId(namespaceId)
        .targetId(targetId)
        .targetGroupId(targetGroupId)
        .build();
  }
}
