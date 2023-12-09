package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.TargetGroup;

@NoArgsConstructor
@Data
public class TargetGroupInsertRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotBlank
  private String name;

  public TargetGroup exportEntity() {
    return TargetGroup.builder()
        .namespaceId(namespaceId)
        .name(name)
        .build();
  }
}
