package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Path;

@NoArgsConstructor
@Data
public class PathInsertRequest {

  private Long namespaceId;
  private String regex;

  public Path exportEntity() {
    return Path.builder()
        .namespaceId(namespaceId)
        .regex(regex)
        .build();
  }
}
