package org.example.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.entity.Role;

@NoArgsConstructor
@Data
public class RoleInsertRequest {

  @NotNull
  @Min(1)
  private Long namespaceId;
  @NotBlank
  private String name;

  public Role exportEntity() {
    return Role.builder()
        .namespaceId(namespaceId)
        .name(name)
        .build();
  }
}
