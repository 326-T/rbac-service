package org.example.web.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Namespace;

@NoArgsConstructor
@Data
public class NamespaceUpdateRequest {

  private String name;

  public Namespace exportEntity() {
    return Namespace.builder()
        .name(name)
        .build();
  }
}
