package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.TargetGroup;

@NoArgsConstructor
@Data
public class TargetGroupUpdateRequest {

  private String name;

  public TargetGroup exportEntity() {
    return TargetGroup.builder()
        .name(name)
        .build();
  }
}
