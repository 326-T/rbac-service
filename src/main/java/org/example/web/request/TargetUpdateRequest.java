package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Target;

@NoArgsConstructor
@Data
public class TargetUpdateRequest {

  private Long namespaceId;
  private String objectIdRegex;

  public Target exportEntity() {
    return Target.builder()
        .namespaceId(namespaceId)
        .objectIdRegex(objectIdRegex)
        .build();
  }
}
