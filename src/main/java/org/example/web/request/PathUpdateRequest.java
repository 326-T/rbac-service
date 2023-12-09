package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Path;

@NoArgsConstructor
@Data
public class PathUpdateRequest {

  @NotBlank
  private String regex;

  public Path exportEntity() {
    return Path.builder()
        .regex(regex)
        .build();
  }
}
