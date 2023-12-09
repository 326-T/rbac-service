package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Target;

@NoArgsConstructor
@Data
public class TargetInsertRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotBlank
  private String objectIdRegex;

  public Target exportEntity() {
    return Target.builder()
        .namespaceId(namespaceId)
        .objectIdRegex(objectIdRegex)
        .build();
  }
}
