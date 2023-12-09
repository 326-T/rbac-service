package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.TargetGroupBelonging;

@NoArgsConstructor
@Data
public class TargetGroupBelongingInsertRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotNull
  @Min(1)
  private Long targetId;
  @NotNull
  @Min(1)
  private Long targetGroupId;

  public TargetGroupBelonging exportEntity() {
    return TargetGroupBelonging.builder()
        .namespaceId(namespaceId)
        .targetId(targetId)
        .targetGroupId(targetGroupId)
        .build();
  }
}
