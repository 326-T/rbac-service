package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Path;

@NoArgsConstructor
@Data
public class PathInsertRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotBlank
  private String regex;

  public Path exportEntity() {
    return Path.builder()
        .namespaceId(namespaceId)
        .regex(regex)
        .build();
  }
}
