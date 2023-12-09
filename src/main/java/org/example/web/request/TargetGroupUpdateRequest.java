package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.TargetGroup;

@NoArgsConstructor
@Data
public class TargetGroupUpdateRequest {

  @NotBlank
  private String name;

  public TargetGroup exportEntity() {
    return TargetGroup.builder()
        .name(name)
        .build();
  }
}
