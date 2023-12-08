package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.TargetGroup;

@NoArgsConstructor
@Data
public class TargetGroupInsertRequest {

  private Long namespaceId;
  private String name;

  public TargetGroup exportEntity() {
    return TargetGroup.builder()
        .namespaceId(namespaceId)
        .name(name)
        .build();
  }
}
