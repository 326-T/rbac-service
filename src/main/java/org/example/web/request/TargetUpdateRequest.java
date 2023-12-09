package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Target;

@NoArgsConstructor
@Data
public class TargetUpdateRequest {

  @NotBlank
  private String objectIdRegex;

  public Target exportEntity() {
    return Target.builder()
        .objectIdRegex(objectIdRegex)
        .build();
  }
}
