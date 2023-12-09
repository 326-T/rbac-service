package org.example.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Namespace;

@NoArgsConstructor
@Data
public class NamespaceInsertRequest {

  @NotBlank
  private String name;

  public Namespace exportEntity() {
    return Namespace.builder()
        .name(name)
        .build();
  }
}
